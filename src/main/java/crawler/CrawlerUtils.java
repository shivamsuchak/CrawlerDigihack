package crawler;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

public class CrawlerUtils {
    public static Boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static String[][] getKeywordPriorities(String url) {
        String[] hrefaboutKeywords = {
                "über-uns", "ueber-uns.html", "about-us", "aboutus", "que-es",
                "über_uns", "ueber_uns", "sobrenos", "uber_uns", "ueber-uns",
                "que_es", "uber-uns", "o-nas", "ueberuns",
                "about.html", "wirueberuns","uber-uns","über uns", "ueber uns", "Über uns", "Ueber uns"
        };
        String[] hrefMainKeywords = {
                "/unternehmen", "a_company", "firma", "die-marke", "die_marke", "company"
        };

        String[] hrefKeywords = {
                cleanAndLowercaseSLD(url),
                "who-we-are", "our-story", "company-info", "profile", "who_we_are", "historie",
                "profil", "overview", "unternehmen","wer_wir_sind", "our-business"
        };

        String[] hrefKeywords2ndTier = {
                "wer-ist", "story", "wir", "our", "somos", "über", "uber", "ueber", "about",
                "facts-and-figures", "welcome-to", "produkte","what_we_do", "what-we-do", "whatwedo", "sortiment"
        };

        return new String[][] {
                hrefaboutKeywords,
                hrefMainKeywords, // First priority
                hrefKeywords,     // Second priority
                hrefKeywords2ndTier // Third priority
        };
    }

    public static String cleanAndLowercaseSLD(String url) {
        try {
            URL parsedURL = new URL(url);
            String host = parsedURL.getHost();
            String[] parts = host.split("\\.");

            if (parts.length >= 2) {
                int lastIndex = parts.length - 1;
                int sldIndex = lastIndex - 1; // Index of the SLD

                String sld = parts[sldIndex];

                // Remove special characters and convert to lowercase
                sld = sld.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

                return sld;
            }
        } catch (MalformedURLException e) {
            return null;
        }

        return null;
    }

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

    public static String convertToHttp(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL should not be null or empty");
        }
        if (!url.startsWith("https://")) {
            throw new IllegalArgumentException("URL should start with https://");
        }
        return url.replaceFirst("https://", "http://");
    }

    public static boolean isValidURL(String url) {
        if (Objects.equals(url, "http://www.http")) {
            return false;
        }
        // Regex to validate the URL structure
        String regex = "^(https?|ftp)://" // start with http, https, or ftp
                + "(([a-zA-Z0-9$\\-_.+!*'(),;?&=]+:)*" // optional user
                + "([a-zA-Z0-9$\\-_.+!*'(),;?&=]+@)?)" // optional password
                + "([a-zA-Z0-9\\-]+\\.)+" // domain (e.g. www.)
                + "([a-zA-Z]{2,6})" // top-level domain (e.g. com)
                + "(:\\d+)?(/[-a-zA-Z0-9$_.+!*'(),%;:@&=]*)*"; // port and path
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        // Check if the URL matches our regex
        if (!matcher.matches()) {
            return false;
        }

        try {
            // This will check the scheme and if the URL is well formed
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            // Malformed URL or unsupported protocol
            return false;
        }

        return true;
    }
    public static Map<Integer, Integer> countAboutUsPagePrioOccurrences(JSONArray jsonArray) {
        Map<Integer, Integer> occurrencesMap = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            // Check if the "aboutUsPagePrio" key exists in the JSON object
            if (jsonObject.has("aboutUsPagePrio")) {
                // Get the JSONArray under "aboutUsPagePrio"
                System.out.println(jsonObject);
                JSONArray aboutUsPagePrioArray = jsonObject.getJSONArray("aboutUsPagePrio");

                // Loop through the values in "aboutUsPagePrio" array
                for (int j = 0; j < aboutUsPagePrioArray.length(); j++) {
                    int priority = aboutUsPagePrioArray.getInt(j);

                    // Update the occurrencesMap
                    occurrencesMap.put(priority, occurrencesMap.getOrDefault(priority, 0) + 1);
                }
            }
        }

        return occurrencesMap;
    }
    static class Pair<F, S> {
        public final F first;
        public final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
      }
    public static String replaceWww3WithWww(String url) {
        return url.replace("www3.", "www.");
    }
}
