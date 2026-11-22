package com.example.fileconversionservice.config;

import com.example.fileconversionservice.exception.ConversionException;
import com.example.fileconversionservice.exception.UnsupportedFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
@Slf4j
public class KafkaErrorHandlingConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(30_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Permanent-ошибки не повторяем даже инфраструктурно
        handler.addNotRetryableExceptions(
                UnsupportedFileTypeException.class,
                ConversionException.class
        );

        // Логирование каждой попытки — для отладки
        handler.setRetryListeners((record, ex, attempt) ->
                log.warn("Retry #{} for {}-{}@{}: {}",
                        attempt,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        ex.getMessage())
        );

        return handler;
    }
}
