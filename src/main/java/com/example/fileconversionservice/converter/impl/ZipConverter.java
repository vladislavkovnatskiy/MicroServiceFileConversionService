package com.example.fileconversionservice.converter.impl;

import com.example.fileconversionservice.converter.Converter;
import com.example.fileconversionservice.converter.ConverterRegistry;
import com.example.fileconversionservice.converter.FileType;
import com.example.fileconversionservice.exception.ConversionException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipConverter implements Converter {

    private final ConverterRegistry registry;

    public ZipConverter(@Lazy ConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean supports(FileType type) {
        return type == FileType.ZIP;
    }

    @Override
    public byte[] convert(byte[] source, String fileName) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(source))) {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            merger.setDestinationStream(out);

            ZipEntry entry;
            boolean hasFiles = false;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                hasFiles = true;
                //TODO: возможно стоит ограничить глубину рекурсии + лимит на суммарный размер (страховка от ZIP_бомбы)
                byte[] pdf = registry.convert(zis.readAllBytes(), entry.getName());
                merger.addSource(new ByteArrayInputStream(pdf).toString());
            }

            if (!hasFiles) {
                throw new ConversionException("ZIP archve is empty: " + fileName);
            }

            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            return out.toByteArray();
        } catch (IOException e) {
            throw new ConversionException("ZIP to PDF failed: " + fileName, e);
        }
    }
}
