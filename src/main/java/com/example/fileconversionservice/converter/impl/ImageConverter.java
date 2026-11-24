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

            // Страница всегда А4
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);

            float margin = 50f;
            float maxW = PDRectangle.A4.getWidth() - 2 * margin;
            float maxH = PDRectangle.A4.getHeight() - 2 * margin;

            // Масштабируем что бы вписать в  maxW x maxH (А4) с сохранением пропорций
            float scale = Math.min(maxW / image.getWidth(), maxH / image.getHeight());
            float drawW =  image.getWidth() * scale;
            float drawH =  image.getHeight() * scale;

            // Централизация на странице
            float x = (PDRectangle.A4.getWidth() - drawW) / 2;
            float y = (PDRectangle.A4.getHeight() - drawH) / 2;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)){
                cs.drawImage(pdImage, x, y, drawW, drawH);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ConversionException("Image to PDF failed: " + fileName, e);
        }
    }
}
