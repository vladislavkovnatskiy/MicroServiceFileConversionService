package com.example.fileconversionservice.kafka;

import com.example.fileconversionservice.entity.OutboxEvent;
import com.example.fileconversionservice.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
            name = "OutboxPublisher_publishPending",
            lockAtMostFor = "PT10M",
            lockAtLeastFor = "PT10S"
    )
    @Transactional
    public void publishPending() {
        List<OutboxEvent> events = outboxRepo
                .findTop100ByStatusOrderByIdAsc(OutboxEvent.OutboxStatus.PENDING);

        if (events.isEmpty()) {
            return;
        }

        log.debug("Publishing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                // Синхронная отправка - нужно знать, что сообщение ушло
                kafkaTemplate.send(event.getTopic(), event.getPayload()).get(5, TimeUnit.SECONDS);

                event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                outboxRepo.save(event);

                log.debug("Outbox event {} published to {}", event.getId(), event.getTopic());
            } catch (Exception e) {
                // Если одно событие не удалось опубликовать — не роняем
                // всю пачку. Логируем, оставляем PENDING, следующий тик
                // попробует снова.
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage(), e);
                // Не break - пробуем остальные события в пачке
            }
        }
    }
}
