package com.example.fileconversionservice.converter.impl;

import com.example.fileconversionservice.converter.Converter;
import com.example.fileconversionservice.converter.FileType;
import com.example.fileconversionservice.exception.ConversionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TxtConverter implements Converter {

    @Override
    public boolean supports(FileType type) {
        return type == FileType.TXT;
    }

    @Override
    public byte[] convert(byte[] source, String fileName) {
        String text = new String(source, StandardCharsets.UTF_8);
        try (PDDocument doc = new PDDocument()) {
            PDFont font;

            try (InputStream fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                if (fontStream == null) {
                    throw new ConversionException("Font /fonts/DejaVuSans.ttf not found in resources");
                }
                font = PDType0Font.load(doc, fontStream, true);
            }

            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(50, 750);
                //TODO: Простой line-by-line без переноса длинных строк.  Если строка длиннее страницы — обрежется. Доработать перенос (разбивать строку по ширине (измерять font.getStringWidth(line) и резать)
                for (String line : text.split("\n", -1)) {
                    cs.showText(sanitize(line));
                    cs.newLineAtOffset(0, -14);
                }
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ConversionException("TXT to PDF failed: " + fileName, e);
        }
    }

    private String sanitize(String line) {
        return line.replace("\t", "    ").replaceAll("\r", "");
    }
}
