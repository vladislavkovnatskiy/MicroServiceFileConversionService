package com.example.fileconversionservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "inbox_events",
        uniqueConstraints = @UniqueConstraint(columnNames = "event_id"),
        indexes = @Index(
                name = "idx_inbox_processed_at",
                columnList = "processed_at"
        )
)
@Getter @Setter
@NoArgsConstructor
public class InboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private Instant processedAt;

    public InboxEvent(UUID eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}
