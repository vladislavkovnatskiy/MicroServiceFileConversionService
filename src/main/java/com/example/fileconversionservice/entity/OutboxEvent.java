package com.example.fileconversionservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = @Index(
                name = "idx_outbox_status_id",
                columnList = "status, id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true)
    private UUID eventId;

    // JSON-сериализованное тело события
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    // Топик, в который нужно опубликовать
    @Column(nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;

    public OutboxEvent(UUID eventId, String payload, String topic) {
        this.eventId = eventId;
        this.payload = payload;
        this.topic = topic;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public enum OutboxStatus {PENDING, PUBLISHED}
}
