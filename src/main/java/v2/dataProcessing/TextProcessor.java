package v2.dataProcessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TextProcessor {
    private ConfigManager.TextConfig textConfig;

    public TextProcessor(ConfigManager.TextConfig textConfig) {
        this.textConfig = textConfig;
    }

    public List<String> processTexts(Document document) {

        List<String> text = pTagsToText(document);

        for (String processingType : textConfig.getProcessingSequence()) {
            switch (processingType) {
                case "removeShortParagraphs":
                    text = removeShortParagraphs(text);
                    break;
                case "splitParagraphsAt":
                    text = splitParagraphsAt(text);
                    break;
                case "removeParagraphsWithKeywords":
                    text = filterParagraphsKeywords(text);
                    break;
                case "removeDuplicatedParagraphs":
                    text = removeDuplicateParagraphs(text);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown TextConfig processing type: " + processingType);
            }
        }
        return text;
    }

    public List<String> pTagsToText(Document document) {
        List<String> pTexts = new ArrayList<>();
        Elements pTags = document.select("p"); // Selects all <p> tags in the document

        for (Element pTag : pTags) {
            pTexts.add(pTag.text()); // Adds the text content of each <p> tag to the list
        }

        return pTexts;
    }

    public List<String> removeShortParagraphs(List<String> text) {
        if (textConfig.removeShortParagraphs <= 0) {
            return text;
        }
        return text.stream()
                .filter(paragraph -> paragraph.length() >= textConfig.removeShortParagraphs)
                .collect(Collectors.toList());
    }

    private List<String> splitParagraphsAt(List<String> text) {
        List<String> parts = new ArrayList<>();

        for (String paragraph : text) {
            int length = paragraph.length();
            for (int i = 0; i < length; i += textConfig.splitParagraphsAt) {
                parts.add(paragraph.substring(i, Math.min(length, i + textConfig.splitParagraphsAt)));
            }
        }
        return parts;
    }

    public List<String> filterParagraphsKeywords(List<String> text) {
        if (textConfig.removeParagraphsWithKeywords == null || textConfig.removeParagraphsWithKeywords.isEmpty()) {
            return text;
        }
        return text.stream()
                .filter(paragraph -> textConfig.removeParagraphsWithKeywords.stream().noneMatch(paragraph::contains))
                .collect(Collectors.toList());
    }

    public List<String> removeDuplicateParagraphs(List<String> text) {
        if (!textConfig.removeDuplicatedParagraphs) {
            return text;
        }
        return new HashSet<>(text).stream().collect(Collectors.toList());
    }



}
