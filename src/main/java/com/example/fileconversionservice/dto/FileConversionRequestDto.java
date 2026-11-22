package com.example.fileconversionservice.dto;

import java.util.UUID;

public record FileConversionRequestDto(UUID eventId, String sourcePath) {
}
