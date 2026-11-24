package com.example.fileconversionservice.repository;

import com.example.fileconversionservice.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboxEventRepository extends JpaRepository<InboxEvent, Long> {
    boolean existsByEventId(UUID eventId);
}