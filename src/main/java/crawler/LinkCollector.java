package crawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import crawler.CrawlerUtils.Pair;

public class LinkCollector {

    public static List<Pair<String, Integer>> processLinks(Elements links, String[][] keywordPriorities) {
        List<Pair<String, Integer>> websitesListWithPriority = new ArrayList<>();
        Set<String> uniqueLinks = new HashSet<>();
        boolean aboutUsFound = false;
        int priority = 0;
        for (String[] keywords : keywordPriorities) {
            priority++;
            for(int j = 0; j<2;j++){
                for (Element link : links) {
                    Element tempLink;
                    if(j==0){
                        tempLink = checkForKeywordPathEnding(link, keywords);
                    } else{
                        tempLink = checkForKeyword(link, keywords);
                    }
                    if (tempLink != null) {
                        String tempString = tempLink.attr("abs:href");
                        if (!uniqueLinks.contains(tempString)) {
                            uniqueLinks.add(tempString);
                            websitesListWithPriority.add(new Pair<>(tempString, priority));
                            if (keywords == keywordPriorities[0] || keywords == keywordPriorities[1]) { // If it's the highest priority
                                aboutUsFound = true;
                                break;
                            }
                            if (websitesListWithPriority.size() >= 3) { // If we've found 3 links
                                aboutUsFound = true;
                                break;
                            }
                        }
                    }
                }
                if (aboutUsFound){
                    break;
                }
            }
            if (aboutUsFound || websitesListWithPriority.size() == 3) {
                break;
            }
        }
        return websitesListWithPriority;
    }

    public static Element checkForRegion(Elements links, Elements paragraphs) {
        for (Element link : links) {
            String href = link.attr("href").toLowerCase();
            String path = getPathFromUrl(href);
            if (containsAny(path, new String[]{"region/germany", "region/eu"})) {
                return link;
            }
        }
        for (Element paragraph : paragraphs) {
            String text = paragraph.text().toLowerCase();
            if (text.contains("please select region")) {
                System.out.println("Error: Language Selection Found but link is not selected!");
                return null;
            }
        }
        return null;
    }

    public static List<String> getWebsitesList(List<Pair<String, Integer>> websitesWithPriority) {
        List<String> websitesList = new ArrayList<>();
        for (Pair<String, Integer> pair : websitesWithPriority) {
            websitesList.add(pair.first);
        }
        return websitesList;
    }

    public static List<Integer> getPrioritiesList(List<Pair<String, Integer>> websitesWithPriority) {
        List<Integer> prioritiesList = new ArrayList<>();
        for (Pair<String, Integer> pair : websitesWithPriority) {
            prioritiesList.add(pair.second);
        }
        return prioritiesList;
    }
    private static Element checkForKeyword(Element link, String[] hrefKeywords) {
        String linkText = link.text().toLowerCase();
        String href = link.attr("href").toLowerCase();
        String path = getPathFromUrl(href);

        // Keywords to exclude (add your exclude keywords here)
        String[] excludeKeywords = {"javascript:void(0)", "mailto:",".pdf" ,".mp4","linkedin","datenschutz","search-results","instagram","facebook","/cart", "/haendlersuche", "google" };

            if (!containsAny(href, excludeKeywords) && (containsAny(path, hrefKeywords) || containsAny(linkText, hrefKeywords))) {
                return link;
            }

        return null;
    }

    private static Element checkForKeywordPathEnding(Element link, String[] hrefKeywords) {
        String linkText = link.text().toLowerCase();
        String href = link.attr("href").toLowerCase();
        String path = getPathFromUrl(href);
        if (path.endsWith("/")) {
            // Remove the trailing "/"
            path = path.substring(0, path.length() - 1);
        }
        // Keywords to exclude (add your exclude keywords here)
        String[] excludeKeywords = {"javascript:void(0)", "mailto:",".pdf" ,".mp4","datenschutz","search-results", "linkedin","instagram","facebook", "/cart","/haendlersuche", "google"};

        for (String keyword : hrefKeywords) {
            if (!containsAny(href, excludeKeywords) && (path.endsWith(keyword))) {
                return link;
            }
        }

        return null;
    }

    // Method to extract path from URL
    private static String getPathFromUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getPath() != null ? uri.getPath().toLowerCase() : "";
        } catch (URISyntaxException e) {
            // Handle the exception as appropriate for your application
            return "";
        }
    }

    // Method to check if the text contains any of the keywords
    private static boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
