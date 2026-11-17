package com.example.fileconversionservice.exception;

// Базовый класс для всех бизнес-ошибок сервиса
public abstract class ApplicationException extends RuntimeException {
    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
