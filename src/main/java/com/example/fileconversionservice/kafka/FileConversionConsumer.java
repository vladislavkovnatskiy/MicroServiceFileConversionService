package com.example.fileconversionservice.kafka;

import com.example.fileconversionservice.dto.FileConversionRequestDto;
import com.example.fileconversionservice.service.FileConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileConversionConsumer {
    private final FileConversionService conversionService;

    @KafkaListener(
            topics = "${app.kafka.request-topic}",
            groupId = "@{spring.kafka.consumer.group-id}"
    )
    public void onMessage(FileConversionRequestDto request) {
        log.debug("Received event: {}", request);
        conversionService.process(request);
    }
}
