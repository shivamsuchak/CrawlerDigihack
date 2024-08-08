package dataScraper;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DataScraper {

    public static boolean isValidURL(String urlStr) {
        try {
            new URL(urlStr).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String scrapeWebPage(JSONArray urls) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        StringBuilder result = new StringBuilder();

        try {
            for (int i = 0; i < urls.length(); i++) {
                String url = urls.getString(i);
                executor.submit(() -> {
                    try {
                        Document document = Jsoup.connect(url).get();
                        Elements footer = document.select("footer");
                        // TODO: Footer might also contain information - check evaluation
                        for (org.jsoup.nodes.Element element : footer) {
                            element.remove();
                        }

                        Elements header = document.select("header");
                        for (org.jsoup.nodes.Element element : header) {
                            element.remove();
                        }

                        Document utf8Document = Jsoup.parse(new String(document.outerHtml().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), url);
                        Elements paragraphs = utf8Document.select("p");
                        List<org.jsoup.nodes.Element> filteredParagraphs = filterParagraph(paragraphs);

                        for (org.jsoup.nodes.Element paragraph : filteredParagraphs) {
                            String cleanedText = paragraph.text().replace("\n", "");
                            result.append(cleanedText).append("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace(); // Handle or log the exception as needed
                    }
                });
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(100000, TimeUnit.MINUTES);
        }

        return result.toString().trim(); // Trim to remove leading/trailing whitespaces
    }
    public static String scrapeWebPageHeaders(String url) throws IOException {
         StringBuilder result = new StringBuilder();
         Document document = Jsoup.connect(url).get();
         Elements footer = document.select("footer");
         // TODO: Footer might also contain information - check evaluation
         for (org.jsoup.nodes.Element element : footer) {
             element.remove();
         }
         Document utf8Document = Jsoup.parse(new String(document.outerHtml().getBytes("UTF-8"), StandardCharsets.UTF_8),
                 url);
         if (url.contains("wiki")) {
             Elements paragraphs = document.select("h1, h2");
             result.append(paragraphs.text().replaceAll("[^A-Za-z0-9 .,!?]", ""));
         }

         Elements paragraphs = utf8Document.select("h1, h2");
         for (org.jsoup.nodes.Element paragraph : paragraphs) {
             result.append(paragraph.text().replaceAll("[^A-Za-z0-9 .,!?]", ""));
         }
         return result.toString();
     }

    public static void main(String[] args) {
        String jsonPath = "data/foundAboutUsPages.json";
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(jsonPath)));
        } catch (IOException e) {
            System.err.println("Error reading the JSON file");
            return;
        }

        JSONArray websites = new JSONArray(jsonContent);
        JSONArray results = new JSONArray();

        for (int i = 0; i < websites.length(); i++) {
            JSONObject website = websites.getJSONObject(i);
            JSONArray aboutUsPages = website.optJSONArray("aboutUsPage");

            if (aboutUsPages != null) {
                    try {
                        String WholeText = scrapeWebPage(aboutUsPages);
                        String[] paragraphs = WholeText.split("\n");

                        JSONObject result = new JSONObject();
                        result.put("WholeText", WholeText);
                        result.put("Paragraphs", new JSONArray(Arrays.asList(paragraphs))); // Convert String array to JSONArray
                        result.put("name", website.getString("name"));
                        result.put("key", website.getString("key"));
                        result.put("website", website.getString("website"));
                        result.put("aboutUsPage", aboutUsPages);
                        result.put("aboutUsPagePrio", website.getJSONArray("aboutUsPagePrio"));
                        result.put("industryCodeList", website.getString("industryCodeList"));
                        results.put(result);

                    } catch (Exception e) {
                        System.err.println("Error scraping the webpage: " + e);

                }
            }
        }

        try (FileWriter file = new FileWriter("data/results.json")) {
            file.write(results.toString(4));
            System.out.println("Successfully written the results to results.json");
        } catch (IOException e) {
            System.err.println("Error writing the results to JSON file");
        }
    }
    
    private static List<Element> filterParagraph(Elements paragraphs) {
        List<Element> usefulParagraphs = new ArrayList<>();
        String[] forbiddenStrings = {"cookies", "telefon", "telefax","@", " straße ","tel.", "tel:", "followers", "linkedin", "©","®", "[mehr]", "fax ", "(m/w/d)", "wählen sie "};
        for (Element paragraph : paragraphs) {
            String text = paragraph.text().trim().toLowerCase();
            // Check if the paragraph is empty or has only one character
            if (!(text.isEmpty()) && text.length() != 1 && !containsAny(text,forbiddenStrings) && !text.matches("\\d+")) {
                usefulParagraphs.add(paragraph);
            }
        }
        // Add more conditions as needed
        return usefulParagraphs;
    }
    public static boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
