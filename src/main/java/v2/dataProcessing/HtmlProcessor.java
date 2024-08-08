package v2.dataProcessing;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlProcessor {

    private ConfigManager.HtmlConfig htmlConfig;

    public HtmlProcessor(ConfigManager.HtmlConfig htmlConfig) {
        this.htmlConfig = htmlConfig;
    }

    public List<Document> processHtml(List<Document> documents) {
        for (String processingType : htmlConfig.getProcessingSequence()) {
            switch (processingType) {
                case "sortPriority":
                    documents = sortDocumentsByPriority(documents);
                    break;
                case "filterDepth":
                    documents = filterByDepthPriority(documents);
                    break;
                case "limitDocuments":
                    documents = limitNumberOfDocuments(documents, htmlConfig.maxSubPages);
                    break;
                case "filterMinimumPriority":
                    documents = filterByMinimumPriority(documents, htmlConfig.minPriorityThreshold);
                    break;
                case "filterIgnoredTags":
                    documents = filterIgnoredTags(documents);
                    break;
                case "rankSubPages":
                    documents = rankSubPages(documents);
                    break;
                default:
            }
        }
        return documents;
    }

    public List<Document> sortDocumentsByPriority(List<Document> documents) {
        Map<String, Integer> urlPriorityKeywords = KeywordManager.getPriorityKeywords("URLPriority");
        return documents.stream()
                .sorted(Comparator.comparing(doc -> calculateKeywordPriority(doc, urlPriorityKeywords), Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private int calculateKeywordPriority(Document document, Map<String, Integer> urlPriorityKeywords) {
        String url = document.location();
        return urlPriorityKeywords.entrySet().stream()
                .filter(entry -> url.contains(entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    public List<Document> filterByDepthPriority(List<Document> documents) {
        if (htmlConfig.shouldConsiderUrlDepth) {
            return documents.stream()
                    .sorted(Comparator.comparing(this::calculateDepthPriority, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }
        return documents;
    }

    private int calculateDepthPriority(Document document) {
        String url = document.location();
        return (int) url.chars().filter(ch -> ch == '/').count();
    }

    public List<Document> limitNumberOfDocuments(List<Document> documents, int maxCount) {
        return documents.stream().limit(maxCount).collect(Collectors.toList());
    }

    public List<Document> filterByMinimumPriority(List<Document> documents, int minPriority) {
        Map<String, Integer> urlPriorityKeywords = KeywordManager.getPriorityKeywords(KeywordManager.URL_PRIORITY);
        return documents.stream()
            .filter(doc -> calculateKeywordPriority(doc, urlPriorityKeywords) >= minPriority)
            .collect(Collectors.toList());
    }

    public List<Document> filterIgnoredTags(List<Document> documents) {
        if (htmlConfig.ignoreTags == null || htmlConfig.ignoreTags.isEmpty()) {
            return documents;
        }
        return documents.stream()
            .filter(doc -> htmlConfig.ignoreTags.stream().noneMatch(tag -> doc.select(tag).isEmpty()))
            .collect(Collectors.toList());
    }

    public List<Document> rankSubPages(List<Document> documents) {
        if (!htmlConfig.rankSubPages || htmlConfig.rankSubPagesBy == null) {
            return documents;
        }
        return documents.stream()
            .sorted((doc1, doc2) -> compareSubPageRank(doc1, doc2, htmlConfig.rankSubPagesBy))
            .collect(Collectors.toList());
    }

    private int compareSubPageRank(Document doc1, Document doc2, Map<Integer, List<String>> rankCriteria) {
        int rank1 = rankCriteria.entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(keyword -> doc1.location().contains(keyword)))
            .mapToInt(Map.Entry::getKey)
            .max().orElse(Integer.MAX_VALUE);
        int rank2 = rankCriteria.entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(keyword -> doc2.location().contains(keyword)))
            .mapToInt(Map.Entry::getKey)
            .max().orElse(Integer.MAX_VALUE);

    return Integer.compare(rank1, rank2);
    }

    public static Set<String> getAllLinksWithKeywords(Document document) {
        List<String> keywords = KeywordManager.getSimpleKeywords(KeywordManager.CRAWLING_KEYWORDS);
        Set<String> uniqueLinks = new HashSet<>();
        String documentBaseUrl = document.baseUri();

        Elements linkElements = document.select("a[href]");
        for (Element link : linkElements) {
            String url = link.attr("abs:href"); // 'abs:href' gives the absolute URL

            // Check if the URL is a standard web page (not a file like PDF, image, etc.)
            if (!url.matches(".*\\.(pdf|jpg|jpeg|png|gif|svg|doc|docx|ppt|pptx|xls|xlsx)$")) {
                if (containsKeyword(url, keywords) && url.startsWith(documentBaseUrl)) {
                    uniqueLinks.add(url);
                }
            }
        }
        return uniqueLinks;
    }

    private static boolean containsKeyword(String url, List<String> keywords) {
        return keywords.stream().anyMatch(url::contains);
    }
}


