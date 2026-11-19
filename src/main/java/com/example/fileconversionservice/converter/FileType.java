package com.example.fileconversionservice.converter;

import com.example.fileconversionservice.exception.UnsupportedFileTypeException;

public enum FileType {
    TXT, PNG, JPG, ZIP;

    public static FileType fromFileName(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() -1) {
            throw new UnsupportedFileTypeException("No extension in file: " +  fileName);
        }
        return switch(fileName.substring(idx + 1).toUpperCase()){
            case "PNG" -> PNG;
            case "JPG", "JPEG" -> JPG;
            case "ZIP" -> ZIP;
            case "TXT" -> TXT;
            default -> throw new UnsupportedFileTypeException("Unsupported extension: " + fileName.substring(idx + 1));
        };
    }
}
