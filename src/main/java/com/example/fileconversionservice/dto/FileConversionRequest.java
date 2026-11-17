package com.example.fileconversionservice.dto;

import java.util.UUID;

public record FileConversionRequest(UUID eventId, String sourcePath) {
}
