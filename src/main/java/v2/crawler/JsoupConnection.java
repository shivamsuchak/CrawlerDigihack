package v2.crawler;

import java.util.concurrent.CompletableFuture;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import v2.crawler.ErrorTracking.ErrorDetail;
import v2.crawler.ErrorTracking.ErrorTracker;
import v2.crawler.Selenium.SeleniumConnection;

/**
 * Provides functionalities to fetch HTML content from a URL using Jsoup with integrated error tracking.
 */
public class JsoupConnection {

    private static final int TIMEOUT = 10000; // Timeout set to 10 seconds
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";

    private CustomCookieStore cookieStore;
    private ErrorTracker errorTracker;

    public JsoupConnection() {
        cookieStore = new CustomCookieStore();
        errorTracker = ErrorTracker.getInstance();
    }

    /**
     * Establishes a connection to the specified URL and fetches the HTML document asynchronously.
     * Tracks errors encountered during the connection and fetching process.
     *
     * @param url The URL to connect to and fetch the HTML document from.
     * @return A CompletableFuture of the HTML document of the specified URL, or null if an error occurs.
     */
    public CompletableFuture<Document> fetchHtmlDocument(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = Jsoup.connect(url)
                        .cookie("Cookie", cookieStore.getCookiesForUrl(url))
                        .timeout(TIMEOUT)
                        .userAgent(USER_AGENT)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate, sdch")
                        .header("Accept-Language", "en-US,en;q=0.8")
                        .header("Connection", "keep-alive")
                        .followRedirects(true)
                        .parser(Parser.xmlParser()); // Use XML parser for better performance

                return connection.execute().parse();
            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName() + " Exception crawling: " + url + ":" + e.getMessage());
                errorTracker.addError(new ErrorDetail("Jsoup", e.getClass().getSimpleName(), url, e.getMessage(), null));
                SeleniumConnection seleniumConnection = new SeleniumConnection();
                Document document = seleniumConnection.getHtmlContent(url);
                if (document != null) {
                    return document;
                }
                return null;
            }
        });
    }
}
