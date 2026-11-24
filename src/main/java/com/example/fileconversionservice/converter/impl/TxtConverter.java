package com.example.fileconversionservice.converter.impl;

import com.example.fileconversionservice.converter.Converter;
import com.example.fileconversionservice.converter.FileType;
import com.example.fileconversionservice.exception.ConversionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TxtConverter implements Converter {

    // Ширина A4 в пунктах (1 pt = 1/72 дюйма)
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    // Поля страницы
    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_TOP = 50f;
    private static final float MARGIN_RIGHT = 50f;
    private static final float MARGIN_BOTTOM = 50f;

    // Доступная ширина для текста
    private static final float MAX_WIDTH = PAGE_WIDTH - MARGIN_RIGHT - MARGIN_LEFT;

    // Размер шрифта и межстрочный интервал
    private static final float FONT_SIZE = 12f;
    private static final float LINE_HEIGHT = 14F;

    // Верхняя Y-координата первой строки (отсчёт от низа страницы)
    private static final float FIRST_LINE_Y = PAGE_HEIGHT - MARGIN_TOP;

    // Нижняя граница - после неё начинаем новую страницу
    private static final float BOTTOM_Y = MARGIN_BOTTOM;

    @Override
    public boolean supports(FileType type) {
        return type == FileType.TXT;
    }

    @Override
    public byte[] convert(byte[] source, String fileName) {
        String text = new String(source, StandardCharsets.UTF_8);

        try (PDDocument doc = new PDDocument()) {
            PDFont font = loadFont(doc);
            List<String> lines = wrapText(text, font);

            // При пустом тексте создаём одну пустую страницу
            // иначе PDF  с нулём страниц неликвиден
            if (lines.isEmpty()) {
                lines = List.of("");
            }

            render(doc, font, lines);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ConversionException("TXT to PDF failed: " + fileName, e);
        }
    }

    private void render(PDDocument doc, PDFont font, List<String> lines) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(doc, page);
        cs.beginText();
        cs.setFont(font, FONT_SIZE);
        cs.newLineAtOffset(MARGIN_LEFT, FIRST_LINE_Y);

        float y = FIRST_LINE_Y;

        for (String line : lines) {
            // Проверка - влезает/нет ещё одна строка
            if (y - LINE_HEIGHT < BOTTOM_Y) {
                cs.endText();
                cs.close();

                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                cs.beginText();
                cs.setFont(font, FONT_SIZE);
                cs.newLineAtOffset(MARGIN_LEFT, FIRST_LINE_Y);
                y = FIRST_LINE_Y;
            }

            if (!line.isEmpty()) {
                cs.showText(line);
            }
            cs.newLineAtOffset(0, -LINE_HEIGHT);
            y -= LINE_HEIGHT;
        }
        cs.endText();
        cs.close();
    }

    private PDFont loadFont(PDDocument doc) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
            if (fontStream == null) {
                throw new ConversionException("Font /fonts/DejaVuSans.ttf not found in resources");
            }
            return PDType0Font.load(doc, fontStream, true);
        }
    }

    private List<String> wrapText(String text, PDFont font) {
        List<String> result = new ArrayList<>();

        for (String logicalLine : text.split("\n", -1)) {

            String sanitized = sanitize(logicalLine);

            // Пустую строку сохраняем как пустую - визуальный отступ
            if (sanitized.isEmpty()) {
                result.add("");
                continue;
            }
            // Разбивка по одному или нескольким пробелам
            String[] words = sanitized.split("\\s+");

            StringBuilder current = new StringBuilder();
            for (String word : words) {
                String candidate = current.isEmpty() ? word : current + " " + word;

                if (textWidth(candidate, font) <= MAX_WIDTH) {
                    // Если влезает, продолжаем набирать текущую строку
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    // Не влезает - сбрасываем текущую строку в результат
                    if (!current.isEmpty()) {
                        result.add(current.toString());
                        current.setLength(0);
                    }
                    // Влезает ли само слобо без соседей на страницу
                    if (textWidth(word, font) <= MAX_WIDTH) {
                        current.append(word);
                    } else {
                        // Слово шире страницы - рвём посимвольно
                        List<String> brokenParts = breakLongWord(word, font);
                        // Все части, кроме последней, уже готовы — в результат.
                        for (int i = 0; i < brokenParts.size() - 1; i++) {
                            result.add(brokenParts.get(i));
                        }
                        // Последняя часть может ещё «потянуть» следующие слова,
                        // поэтому кладём её в current, а не в result.
                        current.append(brokenParts.get(brokenParts.size() - 1));
                    }
                }
            }

            if (!current.isEmpty()) {
                result.add(current.toString());
            }
        }
        return result;
    }

    private String sanitize(String line) {
        return line.replace("\t", "    ").replaceAll("\r", "");
    }

    private List<String> breakLongWord(String word, PDFont font) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            String candidate = current.toString();

            if (textWidth(candidate, font) <= MAX_WIDTH) {
                current.append(c);
            } else {
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                }
                current.setLength(0);
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            parts.add(current.toString());
        }

        return parts;
    }

    private float textWidth(String text, PDFont font) {
        try {
            return font.getStringWidth(text) / 1000f * FONT_SIZE;
        } catch (IOException e) {
            throw new ConversionException("Cannot measure text width: " + text, e);
        }
    }
}
