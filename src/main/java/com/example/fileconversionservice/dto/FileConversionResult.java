package com.example.fileconversionservice.dto;

import java.util.UUID;

public record FileConversionResult(
        UUID eventId,
        boolean success,
        String sourcePath,
        String pdfPath,
        String errorMessage
) {
    public static FileConversionResult success(UUID eventId, String sourcePath, String pdfPath) {
        return new FileConversionResult(eventId, true, sourcePath, pdfPath, null);
    }

    public static FileConversionResult error(UUID eventId, String sourcePath, String errorMessage) {
        return new FileConversionResult(eventId, false, sourcePath, null, errorMessage);
    }
}
