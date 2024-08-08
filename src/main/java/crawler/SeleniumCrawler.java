package crawler;

import java.util.List;

import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import crawler.CrawlerUtils.Pair;

public class SeleniumCrawler {
    public static boolean checkForJavaScript(Document document) {
        return !document.select("noscript").isEmpty();
    }

    public static Pair<String, Integer> findAboutUsPageWithSelenium(String website, String[][] keywordPriorities) {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);

        String foundAboutUsLink = null;
        int priority = 0;

        try {
            driver.get(website);
            List<WebElement> links = driver.findElements(By.tagName("a"));

            // Check links against keyword priorities
            for (String[] keywords : keywordPriorities) {
                priority++;
                for (WebElement link : links) {
                    String href = link.getAttribute("href").toLowerCase();
                    if (CrawlerUtils.containsAny(href, keywords)) {
                        foundAboutUsLink = href;
                        break;
                    }
                }
                if (foundAboutUsLink != null) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while trying to find 'About Us' page with Selenium: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return new Pair<>(foundAboutUsLink, priority);
    }
}
