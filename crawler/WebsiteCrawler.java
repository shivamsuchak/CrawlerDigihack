package v2.crawler;

import static crawler.CrawlerUtils.replaceWww3WithWww;
import static crawler.LinkCollector.checkForRegion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WebsiteCrawler {

    public static Map<String, List<String>> scrapeWebsiteLink(String websiteURL, ExceptionTracker exceptionTracker) throws IOException, HttpStatusException {

        System.out.println("Scraping website: " + websiteURL);

        Map<String, List<String>> data = new HashMap<>();
        websiteURL = replaceWww3WithWww(websiteURL);
        
        websiteURL = checkForLanguageSelection(websiteURL, exceptionTracker);
        Document document = connectToWebsite(websiteURL, exceptionTracker);

        List<String> mainPageParagraphs = extractParagraphsFromDocument(document);
        data.put(websiteURL, mainPageParagraphs);

        Elements links = document.select("a[href]");
        List<String> filteredLinks = filterLinksByPriority(links, CrawlerUtils.getKeywordPriorities(), document.baseUri());

        for (String link : filteredLinks) {
            System.out.println(link);
        }

        for (String link : filteredLinks) {
            Document subDocument = connectToWebsite(link, exceptionTracker);
            List<String> paragraphs = extractParagraphsFromDocument(subDocument); // Use subDocument here
            data.put(link, paragraphs);   
        }
        return data;
    }
    

    /**
     * Checks a website for language selection options.
     * 
     * @param website The URL of the website to check.
     * @return The URL of the language selection page, or the original URL if none found.
     */
    private static String checkForLanguageSelection(String website, ExceptionTracker exceptionTracker) {
        Document document = connectToWebsite(website, exceptionTracker);
        Elements links = document.select("a[href]");
        Elements text = document.select("p");

        // Check for language region in the links and text
        Element languageSelection = checkForRegion(links, text);
        if (languageSelection != null) {
            System.out.println("Language selection found.");
            return languageSelection.attr("abs:href");
        }
        return website;
    }

    /**
     * Connects to the given website and returns the response as a Document.
     *
     * @param website The website to connect to.
     * @return The response as a Document.
     * @throws Exception If an error occurs while connecting to the website.
     */
    private static Document connectToWebsite(String website, ExceptionTracker exceptionTracker) {

        int TIMEOUT = 10000;
        int maxRetries = 3; // Set the maximum number of retries
        int attempt = 0; // Current attempt count

        while (maxRetries >= attempt) {
            attempt++;
            try {
                Response response = Jsoup.connect(website)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate, sdch")
                        .header("Accept-Language", "en-US,en;q=0.8")
                        .header("Connection", "keep-alive")
                        .followRedirects(true)
                        .execute();
    
            // TODO: Handle redirects properly and make sure the final URL is used
    
            // Parse the response to a Document
            return response.parse();
            } catch (HttpStatusException hse) {
                // TODO: selenium
                System.out.println("HTTP Status Exception: " + hse.getStatusCode() + " " + website);
                exceptionTracker.addHttpException(hse.getStatusCode(), website);
            } catch (SSLHandshakeException e) {
                System.out.println("SSL Handshake Exception: " + website);
                // website = convertToHttp(website);
            } catch (SSLException e) {
                System.out.println("SSL Exception: " + website);
                // website = convertToHttp(website);
            } catch (IOException e) {
                System.out.println("IO Exception: " + website);
                exceptionTracker.addIoException(e.getMessage(), website);
            }
        }
        // Return an empty Document if all retries fail
        return new Document("");
    }
    
    
public static List<String> filterLinksByPriority(Elements links, Map<String, Integer> keywords, String baseUri) {
    Set<String> nonSupportedExtensions = new HashSet<>(Arrays.asList(".pdf", ".doc", ".docx", ".exe", ".zip", ".rar", ".tar", ".gz", ".7z", ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif", ".svg", ".svgz", ".mp3", ".mp4", ".avi", ".mov", ".flv", ".wmv", ".wma", ".ogg", ".wav", ".m4a", ".aac", ".flac", ".ape", ".alac", ".aiff", ".aif", ".wv", ".mka", ".opus", ".webm", ".m3u", ".m3u8", ".pls", ".cu", "ashx"));

    // Create a sorted list of keywords based on their priorities (higher priority first)
    List<Map.Entry<String, Integer>> sortedKeywords = keywords.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toList());

    // Use a LinkedHashSet to maintain order while removing duplicates
    return links.stream()
        .map(link -> link.absUrl("href")) // Directly get absolute URL
        .filter(linkHref -> nonSupportedExtensions.stream().noneMatch(linkHref::endsWith)) // Filter out non-supported MIME types
        .filter(linkHref -> linkHref.startsWith(baseUri)) // Filter based on baseUri
        .filter(linkHref -> sortedKeywords.stream()
            .anyMatch(keywordEntry -> linkHref.contains(keywordEntry.getKey())))
        .collect(Collectors.toCollection(LinkedHashSet::new)) // Collect unique links while preserving order
        .stream()
        .collect(Collectors.toList()); // Convert to List
}


    /**
     * Extracts all paragraphs from the given Jsoup Document.
     *
     * @param document The Jsoup Document to extract paragraphs from.
     * @return A List of Strings containing the extracted paragraphs.
     */
    public static List<String> extractParagraphsFromDocument(Document document) {
        List<String> paragraphs = new ArrayList<>();

        Elements pTags = document.select("p");
        for (Element p : pTags) {
            if (p.text().length() > 0)
                paragraphs.add(p.text());
        }
        return paragraphs;
    }
}
    