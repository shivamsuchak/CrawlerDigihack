package v2.crawler;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;

import v2.dataProcessing.HtmlProcessor;

/**
 * A web crawler that fetches web pages and their relevant data.
 * It uses multi-threading and asynchronous calls for efficient crawling of web resources.
 */
public class Crawler {

    private static final int NUMBER_OF_THREADS = 50; // Number of threads in the thread pool
    private ExecutorService executorService; // Executor service for managing threads

    /**
     * Constructor for Crawler.
     * Initializes the thread pool.
     */
    public Crawler() {
        this.executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    /**
     * Crawls the full page starting from a base URL.
     * It fetches the base page and then all linked pages within it asynchronously.
     *
     * @param baseUrl The base URL from which to start crawling.
     * @return A CompletableFuture of a list of Document objects representing all fetched pages.
     */
    public CompletableFuture<List<Document>> crawlFullPage(String baseUrl) {
        CompletableFuture<List<Document>> result = new CompletableFuture<>();
        JsoupConnection jsoupConnection = new JsoupConnection();

        jsoupConnection.fetchHtmlDocument(baseUrl).thenAccept(baseDocument -> {
            if (baseDocument != null) {
                Set<String> links = HtmlProcessor.getAllLinksWithKeywords(baseDocument);
                List<CompletableFuture<Document>> futures = links.stream()
                        .map(jsoupConnection::fetchHtmlDocument)
                        .collect(Collectors.toList());

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept(v -> {
                    List<Document> documents = futures.stream()
                        .map(CompletableFuture::join) // This will block until all futures complete
                        .filter(Objects::nonNull) // Filter out null documents
                        .collect(Collectors.toList());
                    documents.add(0, baseDocument); // Add the base document at the beginning
                    result.complete(documents);
                }).exceptionally(ex -> {
                    // Log and handle the exception silently
                    result.complete(Collections.singletonList(baseDocument)); // Return only the base document
                    return null;
                });
            } else {
                // Handle the failure silently
                System.out.println("Failed to fetch base document: " + baseUrl);

                result.complete(Collections.emptyList());
            }
        }).exceptionally(ex -> {
            // Handle the exception silently
            System.out.println("Failed to fetch base document: " + baseUrl + " - " + ex.getMessage());
            result.complete(Collections.emptyList());
            return null;
        });
        return result;
    }

    /**
     * Closes the executor service and releases resources.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
