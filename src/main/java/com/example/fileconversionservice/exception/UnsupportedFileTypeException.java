package com.example.fileconversionservice.exception;

// Постоянная ошибка: повтор не поможет - плохой файл, неподдерживаемый формат
public class UnsupportedFileTypeException extends ApplicationException {
    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
