package com.example.fileconversionservice.converter;

public interface Converter {

    boolean supports(FileType type);

    byte[] convert(byte[] bytes, String fileName);
}
