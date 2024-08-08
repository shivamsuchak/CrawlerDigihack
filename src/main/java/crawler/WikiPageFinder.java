package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WikiPageFinder {

    public static String  getCompanyName(String website) {
        String companyName;

        if (website.contains("www")) {
            companyName = StringUtils.substringBetween(website, "www.", ".");
        } else {
            companyName = StringUtils.substringBetween(website, "//", ".");
        }

        if (companyName.contains("-")) {
            String[] parts = companyName.split("-");
            StringBuilder result = new StringBuilder();

            for (String part : parts) {
                if (!result.isEmpty()) {
                    result.append("_");
                }
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }

            companyName = result.toString();
        } else {
            companyName = Character.toUpperCase(companyName.charAt(0)) + companyName.substring(1);
        }

        return companyName;
    }


    public static boolean checkWikiURLExistence(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");  // Use a HEAD request to minimize data transfer
            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getWikiPage(String website){
        String company= getCompanyName(website);

        String wikiURLen = "https://en.wikipedia.org/wiki/" + company;
        String wikiURLde = "https://de.wikipedia.org/wiki/" + company;

        if (checkWikiURLExistence(wikiURLen))
            return wikiURLen;

        else if (checkWikiURLExistence(wikiURLde))
            return wikiURLde;

        else
            return null;

    }


    public static String getWikiPageText(String website) {
        try {
            // Connect to the URL and get the HTML document
            Document document = Jsoup.connect(website).get();

            // Get the text content from <p> tags
            Elements pTags = document.select("p");
            StringBuilder pTextContent = new StringBuilder();

            // Counter to keep track of the number of h2 tags found
            int h2Counter = 0;

            for (org.jsoup.nodes.Element element : document.select("*")) {
                // Check if the current element is an h2 tag
                if (element.tagName().equals("h2")) {
                    // Increment the counter
                    h2Counter++;

                    // Check if we have found the second h2 tag
                    if (h2Counter == 3) {
                        break;  // Stop extracting text
                    }
                }

                // Check if the current element is a p tag
                if (element.tagName().equals("p")) {
                    // Append the text content to the StringBuilder
                    pTextContent.append(element.text()).append("\n");
                }
            }
            return pTextContent.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}