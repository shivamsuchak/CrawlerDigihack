package v2.crawler;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CrawlerUtils {

    private static final Map<String, Integer> KEYWORD_PRIORITIES = createKeywordPriorityMap();

    private static Map<String, Integer> createKeywordPriorityMap() {
        Map<String, Integer> map = new HashMap<>();
        
        // Priority 1 keywords
        String[] prio1Keywords = {
            "über-uns", "ueber-uns.html", "about-us", "aboutus", "que-es",
            "über_uns", "ueber_uns", "sobrenos", "uber_uns", "ueber-uns",
            "que_es", "uber-uns", "o-nas", "ueberuns",
            "about.html", "wirueberuns", "uber-uns", "über uns", "ueber uns", "Über uns", "Ueber uns"
        };
        for (String keyword : prio1Keywords) {
            map.put(keyword, 1);
        }

        // Priority 2 keywords
        String[] prio2Keywords = {
            "/unternehmen", "a_company", "firma", "die-marke", "die_marke", "company"
        };
        for (String keyword : prio2Keywords) {
            map.put(keyword, 2);
        }

        // Priority 3 keywords
        String[] prio3Keywords = {
            "who-we-are", "our-story", "company-info", "profile", "who_we_are", "historie",
            "profil", "overview", "unternehmen", "wer_wir_sind", "our-business"
        };
        for (String keyword : prio3Keywords) {
            map.put(keyword, 3);
        }

        // Priority 4 keywords
        String[] prio4Keywords = {
            "wer-ist", "story", "wir", "our", "somos", "über", "uber", "ueber", "about",
            "facts-and-figures", "welcome-to", "produkte", "what_we_do", "what-we-do", "whatwedo", "sortiment"
        };
        for (String keyword : prio4Keywords) {
            map.put(keyword, 4);
        }

        return map;
    }

    public static Map<String, Integer> getKeywordPriorities() {
        return KEYWORD_PRIORITIES;
    }

    public static boolean isValidURL(String urlStr) {
    try {
        new URL(urlStr).toURI();
        return true;
    } catch (Exception e) {
        return false;
        }
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
}


