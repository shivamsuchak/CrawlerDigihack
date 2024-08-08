package crawler;

import static crawler.CrawlerUtils.convertToHttp;
import static crawler.CrawlerUtils.countAboutUsPagePrioOccurrences;
import static crawler.CrawlerUtils.getKeywordPriorities;
import static crawler.CrawlerUtils.replaceWww3WithWww;
import static crawler.LinkCollector.checkForRegion;
import static crawler.LinkCollector.getPrioritiesList;
import static crawler.LinkCollector.getWebsitesList;
import static crawler.LinkCollector.processLinks;
import static crawler.SeleniumCrawler.checkForJavaScript;
import static crawler.SeleniumCrawler.findAboutUsPageWithSelenium;
import static crawler.URLCleaner.cleanURL;
import static dataScraper.DataScraper.isValidURL;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import crawler.CrawlerUtils.Pair;
import parser.JsonParser;

public class AboutUsPageCrawler {

    // Extracted function for scraping website links
    private static JSONObject scrapeWebsiteLinks(JSONObject jsonObject, ExceptionTracker exceptionTracker) {
        int maxRetries = 0;
        String website = jsonObject.getString("website");
        website = replaceWww3WithWww(website);
        JSONObject resultObject = new JSONObject();
        resultObject.put("name", jsonObject.getString("name"));
        resultObject.put("key", jsonObject.getString("key"));
        resultObject.put("website", website);
        resultObject.put("industryCodeList", jsonObject.getString("industryCodeList"));

        if (!isValidURL(website)) {
            resultObject.put("aboutUsPage", "invalid - website from JSON");
            exceptionTracker.addInvalidUrl(website);
            return resultObject;
        }
        website = CheckForLanguageSelection(website);
        System.out.println(website);
        while (maxRetries < 2) {
            maxRetries++;
            try {
                Document document = Jsoup.connect(website)
                        .timeout(10000)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate, sdch")
                        .header("Accept-Language", "en-US,en;q=0.8")
                        .header("Connection", "keep-alive")
                        .get();

                Elements links = document.select("a[href]");
                List<Pair<String, Integer>> processedList = processLinks(links, getKeywordPriorities(website));
                List<String> websitesList = getWebsitesList(processedList);
                List<Integer> priorityList = getPrioritiesList(processedList);

                if (links.isEmpty()) {
                    resultObject.put("aboutUsPage", website);
                    break;
                }

                int websitesFound = websitesList.size();

                if (websitesFound == 0) {
                    if (checkForJavaScript(document)) {
                        Pair<String, Integer> aboutUsPageSelenium = findAboutUsPageWithSelenium(website, getKeywordPriorities(website));

                        if (aboutUsPageSelenium.first != null) {
                            System.out.println("ABOUT US PAGE FOUND WITH SELENIUM " + aboutUsPageSelenium.first);
                            resultObject.put("aboutUsPage", aboutUsPageSelenium.first);
                            JSONArray jsonArrayPrioSelenenium = new JSONArray().put(aboutUsPageSelenium.second);
                            resultObject.put("aboutUsPagePrio", jsonArrayPrioSelenenium);
                        } else {
                            resultObject.put("aboutUsPage", website);
                        }
                    } else {
                        resultObject.put("aboutUsPage", website);
                    }
                } else {
                    resultObject.put("aboutUsPage", websitesList);
                    resultObject.put("aboutUsPagePrio", priorityList);
                }

            } catch (HttpStatusException hse) {
                maxRetries = 2;
                Pair<String, Integer> aboutUsPageSelenium = findAboutUsPageWithSelenium(website, getKeywordPriorities(website));

                if (aboutUsPageSelenium.first != null) {
                    System.out.println("HttpStatusException found with Selenium " + aboutUsPageSelenium.first);
                    resultObject.put("aboutUsPage", aboutUsPageSelenium.first);
                    JSONArray jsonArrayPrioSelenenium = new JSONArray().put(aboutUsPageSelenium.second);
                    resultObject.put("aboutUsPagePrio", jsonArrayPrioSelenenium);
                }
                else {
                    resultObject.put("aboutUsPage", "invalid - HttpStatusException");
                    exceptionTracker.addHttpStatusException(website);
                }
            } catch (SSLHandshakeException e) {
                website = convertToHttp(website);
            } catch (SSLException e) {
                website = convertToHttp(website);
            } catch (SocketTimeoutException e) {
                maxRetries = 2;
                exceptionTracker.addSocketTimeout(website);
                resultObject.put("aboutUsPage", "invalid - Timeout");
            } catch (UnknownHostException e) {
                maxRetries = 2;
                exceptionTracker.addUnknownHostException(website);
                resultObject.put("aboutUsPage", "invalid - Unknown Host");
            } catch (SocketException e) {
                maxRetries = 2;
                Pair<String, Integer> aboutUsPageSelenium = findAboutUsPageWithSelenium(website, getKeywordPriorities(website));

                if (aboutUsPageSelenium.first != null) {
                    System.out.println("Socket Exception found with Selenium " + aboutUsPageSelenium.first);
                    resultObject.put("aboutUsPage", aboutUsPageSelenium.first);
                    JSONArray jsonArrayPrioSelenenium = new JSONArray().put(aboutUsPageSelenium.second);
                    resultObject.put("aboutUsPagePrio", jsonArrayPrioSelenenium);
                }
                else {
                    resultObject.put("aboutUsPage", "invalid - Socket");
                    exceptionTracker.addSocketException(website);
                }
            } catch (IOException e) {
                maxRetries = 2;
                exceptionTracker.addInputOutputException(website);
                resultObject.put("aboutUsPage", "invalid - Exception");
            }
        }

        return resultObject;
    }

    private static String CheckForLanguageSelection(String website) {
        int maxRetries = 0;
        while (maxRetries < 2) {
            maxRetries++;
            try {
                Document document = Jsoup.connect(website)
                        .timeout(10000)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate, sdch")
                        .header("Accept-Language", "en-US,en;q=0.8")
                        .header("Connection", "keep-alive")
                        .get();

                Elements links = document.select("a[href]");
                Elements text = document.select("p");
                if (checkForRegion(links, text) != null) {
                    System.out.println("language selection");
                    return checkForRegion(links, text).attr("abs:href");
                }

            } catch (HttpStatusException hse) {
                maxRetries = 2;
            } catch (SSLHandshakeException e) {
                website = convertToHttp(website);
            } catch (SSLException e) {
                website = convertToHttp(website);
            } catch (SocketTimeoutException e) {
                maxRetries = 2;
            } catch (UnknownHostException e) {
                maxRetries = 2;
            } catch (SocketException e) {
                maxRetries = 2;
            } catch (Exception e) {
                maxRetries = 2;
            }
        }
        return website;
    }
        public static void main (String[]args){
            String jsonFile = "data/websites1500.json";
            ExceptionTracker exceptionTracker = new ExceptionTracker();
            JSONArray resultArray = new JSONArray();
            ExecutorService executor = Executors.newFixedThreadPool(100);

            try {
                String strJson = JsonParser.getJSONFromFile(jsonFile);
                JSONArray jsonArray = new JSONArray(strJson);
                JSONArray cleanedJsonArray = cleanURL(jsonArray);

                for (int i = 0; i < cleanedJsonArray.length(); i++) {
                    final int index = i;
                    executor.submit(() -> {
                        JSONObject resultObject = scrapeWebsiteLinks(cleanedJsonArray.getJSONObject(index), exceptionTracker);
                        synchronized (resultArray) {
                            resultArray.put(resultObject);
                        }
                    });
                }

                executor.awaitTermination(10, TimeUnit.MINUTES);
                executor.shutdown();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/foundAboutUsPages.json"))) {
                    writer.write(resultArray.toString(4));
                }
                exceptionTracker.printExceptions();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            synchronized (resultArray) {
                Map<Integer, Integer> occurrencesMap = countAboutUsPagePrioOccurrences(resultArray);

                // Print the results
                for (Map.Entry<Integer, Integer> entry : occurrencesMap.entrySet()) {
                    System.out.println("AboutUsPagePrio " + entry.getKey() + ": " + entry.getValue() + " occurrences");
                }
                exceptionTracker.printExceptions();
            }
        }
    }
