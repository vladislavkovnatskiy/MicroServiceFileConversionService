package com.example.fileconversionservice.exception;

// Постоянная ошибка: конвертация невозможна -битый файл/изображение, нет шрифта
public class ConversionException extends ApplicationException {
    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
