package com.example.fileconversionservice.service;

import com.example.fileconversionservice.converter.ConverterRegistry;
import com.example.fileconversionservice.dto.FileConversionRequestDto;
import com.example.fileconversionservice.dto.FileConversionResultDto;
import com.example.fileconversionservice.entity.InboxEvent;
import com.example.fileconversionservice.entity.OutboxEvent;
import com.example.fileconversionservice.exception.ConversionException;
import com.example.fileconversionservice.exception.UnsupportedFileTypeException;
import com.example.fileconversionservice.repository.InboxEventRepository;
import com.example.fileconversionservice.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileConversionService {

    private final InboxEventRepository inboxRepo;
    private final OutboxEventRepository outboxRepo;
    private final StorageService storageService;
    private final ConverterRegistry  converterRegistry;
    private final JsonMapper jsonMapper;

    // Топик, куда публикуем результат
    @Value("${app.kafka.result-topic}")
    private String getResultTopic;

    @Transactional
    public void process(FileConversionRequestDto request) {

        if (inboxRepo.existsByEventId(request.eventId())) {
            log.info("Event {} already processed, skipping", request.eventId());
            return;
        }

        FileConversionResultDto result;
        try {

            byte[] source = storageService.read(request.sourcePath());
            byte[] pdf = converterRegistry.convert(source, request.sourcePath());

            // Пишем pdf в S3
            String pdPath = "output/" + request.eventId() + ".pdf";
            storageService.write(pdPath, pdf, "application/pdf");

            result = FileConversionResultDto.success(request.eventId(), request.sourcePath(), pdPath);

        } catch (UnsupportedFileTypeException | ConversionException e){

            log.warn("Permanent conversion failure for event {}: {}", request.eventId(), e.getMessage(), e);
            result = FileConversionResultDto.error(request.eventId(), request.sourcePath(), e.getMessage());
        }

        saveOutbox(result);
        inboxRepo.save(new InboxEvent(request.eventId()));

        log.info("Event {} processed: success={}", request.eventId(), result.success());
    }

    private void saveOutbox(FileConversionResultDto result) {
        try {
            String payload = jsonMapper.writeValueAsString(result);
            outboxRepo.save(new OutboxEvent(result.eventId(), payload, getResultTopic));
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize result for event " + result.eventId(), e);
        }
    }
}
