package com.example.fileconversionservice.dto;

import java.util.UUID;

public record FileConversionResultDto(
        UUID eventId,
        boolean success,
        String sourcePath,
        String pdfPath,
        String errorMessage
) {
    public static FileConversionResultDto success(UUID eventId, String sourcePath, String pdfPath) {
        return new FileConversionResultDto(eventId, true, sourcePath, pdfPath, null);
    }

    public static FileConversionResultDto error(UUID eventId, String sourcePath, String errorMessage) {
        return new FileConversionResultDto(eventId, false, sourcePath, null, errorMessage);
    }
}
