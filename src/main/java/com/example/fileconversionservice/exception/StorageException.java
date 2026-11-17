package com.example.fileconversionservice.exception;

// Временная ошибка: сеть, недоступность хранилища
// Kafka повторит
public class StorageException extends ApplicationException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
