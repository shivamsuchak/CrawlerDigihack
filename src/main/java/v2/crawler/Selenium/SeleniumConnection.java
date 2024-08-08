package v2.crawler.Selenium;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;
import v2.crawler.ErrorTracking.ErrorDetail;
import v2.crawler.ErrorTracking.ErrorTracker;

public class SeleniumConnection {

    private ErrorTracker errorTracker;

    public SeleniumConnection() {
        this.errorTracker = ErrorTracker.getInstance();
    }

    public Document getHtmlContent(String url) {
        WebDriver driver = null;
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1200");

            driver = new ChromeDriver(options);
            driver.get(url);
            String pageSource = driver.getPageSource();
            return Jsoup.parse(pageSource);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            errorTracker.addError(new ErrorDetail("Selenium", e.getClass().getSimpleName(), url, e.getMessage(), null));
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public static void main(String[] args) {
        String url = "https://www.vogt-gmbh.de"; // Replace with the URL you want to fetch

        SeleniumConnection seleniumConnection = new SeleniumConnection();
        Document htmlContent = seleniumConnection.getHtmlContent(url);

        if (htmlContent != null) {
            System.out.println("HTML Content:");
            System.out.println(htmlContent.toString());
        } else {
            System.out.println("Failed to fetch HTML content.");
        }
    }
}
