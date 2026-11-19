package com.example.fileconversionservice.converter.impl;

import com.example.fileconversionservice.converter.Converter;
import com.example.fileconversionservice.converter.FileType;
import com.example.fileconversionservice.exception.ConversionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ImageConverter implements Converter {

    @Override
    public boolean supports(FileType type) {
        return type == FileType.PNG || type == FileType.JPG;
    }

    @Override
    public byte[] convert(byte[] source, String fileName) {
        try (PDDocument doc = new PDDocument();
             ByteArrayInputStream in = new ByteArrayInputStream(source)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new ConversionException("Cannot read image: " + fileName);
            }

            //TODO: Размер страницы = размер изображения, страница PDF будет размером с изображение (например, 4000×3000 px). Попробовать A4 (PDRectangle.A4) + масштабирование под страницу.
            PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
            doc.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ConversionException("Image to PDF failed: " + fileName, e);
        }
    }
}
