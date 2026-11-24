package com.example.fileconversionservice.converter;

import com.example.fileconversionservice.exception.UnsupportedFileTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConverterRegistry {

    private final List<Converter> converters;

    public byte[] convert(byte[] source, String fileName) {
        FileType type = FileType.fromFileName(fileName);
        return converters.stream()
                .filter(c -> c.supports(type))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileTypeException(
                        "No converter for type: " + type))
                .convert(source, fileName);
    }
}
