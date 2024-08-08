package v2.dataProcessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordManager {

    public static final String CRAWLING_KEYWORDS = "crawlingKeywords";
    public static final String URL_PRIORITY = "URLPriority";

    private static Map<String, Map<String, Integer>> priorityKeywordLists;
    private static Map<String, List<String>> simpleKeywordLists;

    public KeywordManager() {
        KeywordManager.priorityKeywordLists = new HashMap<>();
        KeywordManager.simpleKeywordLists = new HashMap<>();
        initUrlPriorityKeywords();
        initCrawlingKeywords();
    }

    public static void addPriorityKeywordList(String listName, Map<String, Integer> keywords) {
        priorityKeywordLists.put(listName, keywords);
    }

    public static void addPriorityKeyword(String listName, String keyword, int priority) {
        priorityKeywordLists.computeIfAbsent(listName, k -> new HashMap<>()).put(keyword, priority);
    }

    public static void removePriorityKeyword(String listName, String keyword) {
        if (priorityKeywordLists.containsKey(listName)) {
            priorityKeywordLists.get(listName).remove(keyword);
        }
    }

    public static Map<String, Integer> getPriorityKeywords(String listName) {
        return new HashMap<>(priorityKeywordLists.getOrDefault(listName, new HashMap<>()));
    }

    public static boolean containsPriorityKeyword(String listName, String keyword) {
        return priorityKeywordLists.containsKey(listName) && priorityKeywordLists.get(listName).containsKey(keyword);
    }

    public static void addSimpleKeywordList(String listName, List<String> keywords) {
        simpleKeywordLists.put(listName, keywords);
    }

    public static void addSimpleKeyword(String listName, String keyword) {
        simpleKeywordLists.computeIfAbsent(listName, k -> new ArrayList<>()).add(keyword);
    }

    public static void removeSimpleKeyword(String listName, String keyword) {
        if (simpleKeywordLists.containsKey(listName)) {
            simpleKeywordLists.get(listName).remove(keyword);
        }
    }

    public static List<String> getSimpleKeywords(String listName) {
        return new ArrayList<>(simpleKeywordLists.getOrDefault(listName, new ArrayList<>()));
    }

    public boolean containsSimpleKeyword(String listName, String keyword) {
        return simpleKeywordLists.containsKey(listName) && simpleKeywordLists.get(listName).contains(keyword);
    }

    private void initUrlPriorityKeywords() {
        Map<String, Integer> priorityMap = new HashMap<>();

        // Priority 1 Keywords
        String[] priority1Keywords = {
            "über-uns", "ueber-uns.html", "about-us", "aboutus", "que-es",
            "über_uns", "ueber_uns", "sobrenos", "uber_uns", "ueber-uns",
            "que_es", "uber-uns", "o-nas", "ueberuns", "about.html", "wirueberuns",
            "uber-uns", "über uns", "ueber uns", "Über uns", "Ueber uns"
        };
        for (String keyword : priority1Keywords) {
            priorityMap.put(keyword, 1);
        }

        // Priority 2 Keywords
        String[] priority2Keywords = {
            "/unternehmen", "a_company", "firma", "die-marke", "die_marke", "company"
        };
        for (String keyword : priority2Keywords) {
            priorityMap.put(keyword, 2);
        }

        // Priority 3 Keywords
        String[] priority3Keywords = {
            "who-we-are", "our-story", "company-info", "profile",
            "who_we_are", "historie", "profil", "overview", "unternehmen", "wer_wir_sind",
            "our-business"
        };
        for (String keyword : priority3Keywords) {
            priorityMap.put(keyword, 3);
        }

        // Priority 4 Keywords
        String[] priority4Keywords = {
            "wer-ist", "story", "wir", "our", "somos", "über", "uber", "ueber", "about",
            "facts-and-figures", "welcome-to", "produkte", "what_we_do", "what-we-do",
            "whatwedo", "sortiment"
        };
        for (String keyword : priority4Keywords) {
            priorityMap.put(keyword, 4);
        }
        addPriorityKeywordList(URL_PRIORITY, priorityMap);
    }

    private void initCrawlingKeywords() {
        String listName = CRAWLING_KEYWORDS;
        ArrayList<String> crawlingKeywords = new ArrayList<String>(
            Arrays.asList(
                "über-uns", "ueber-uns.html", "about-us", "aboutus", "que-es", "über_uns", "ueber_uns", "sobrenos", "uber_uns", "ueber-uns",
                "que_es", "uber-uns", "o-nas", "ueberuns", "about.html", "wirueberuns",
                "uber-uns", "über uns", "ueber uns", "Über uns", "Ueber uns",
                "/unternehmen", "a_company", "firma", "die-marke", "die_marke", "company",
                "who-we-are", "our-story", "company-info", "profile", "who_we_are",
                "historie", "profil", "overview", "unternehmen", "wer_wir_sind",
                "our-business", "wer-ist", "story", "wir", "our", "somos", "über",
                "uber", "ueber", "about", "facts-and-figures", "welcome-to",
                "produkte", "what_we_do", "what-we-do", "whatwedo", "sortiment"
            )
        );

        addSimpleKeywordList(listName, crawlingKeywords);
    }

    }
