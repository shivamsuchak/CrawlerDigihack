package dataScraper;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DataAnalysis {

    public Boolean analyseObject(JSONObject entry) {

            // Initialize a list to store results
            List<JSONObject> results = new ArrayList<>();
            String[] keywords = {"companyname", " wir ", " we ", " unternehmen "};

            // Define the thresholds for scoring
            int lowerThresholdMedianWordsPerParagraph = 5;
            int upperThresholdMedianWordsPerParagraph = 100;
            int higherThresholdMedianWordsPerParagraph = 150;

            int lowestThresholdWordsTotal = 8;
            int lowerThresholdWordsTotal = 20;
            int upperThresholdWordsTotal = 400;
            int higherThresholdWordsTotal = 800;

            int lowerThresholdNumParagraphs = 0;
            int upperThresholdNumParagraphs = 10;
            int higherThresholdNumParagraphs = 15;

            String wholeText = (String) entry.get("WholeText");
            JSONArray paragraphs = (JSONArray) entry.get("Paragraphs");

            // Calculate median number of words per paragraph
            long medianWordsPerParagraph = calculateMedian(paragraphs);

            // Calculate total number of words in the "WholeText"
            long wordsTotal = calculateWordsTotal(wholeText);

            // Get the number of paragraphs
            long numParagraphs = paragraphs.size();

            // Get the length of the path
            JSONArray path = (JSONArray) entry.get("aboutUsPagePrio");

            int scoreMedianWordsPerParagraph = calculateScore(lowerThresholdMedianWordsPerParagraph,
                    upperThresholdMedianWordsPerParagraph, higherThresholdMedianWordsPerParagraph, medianWordsPerParagraph);

            int scoreWordsTotal = (wordsTotal > lowestThresholdWordsTotal) ?
                    calculateScore(lowerThresholdWordsTotal, upperThresholdWordsTotal, higherThresholdWordsTotal, wordsTotal) :
                    0;


            int scoreNumParagraphs = calculateScore(lowerThresholdNumParagraphs, upperThresholdNumParagraphs,
                    higherThresholdNumParagraphs, numParagraphs);

            int scorePath = (path != null && !path.isEmpty()) ? ((Long) path.get(0)).intValue() * 100 - 100 : 0;

            int scoreKeywords = calculateKeywordScore(keywords, wholeText);

            int finalScore = scoreMedianWordsPerParagraph + scoreWordsTotal + scoreNumParagraphs + scorePath;

            String isGoodData = (finalScore < 300) ? "Good Data" : "Bad Data";

            // Your logic for handling the result goes here
            return isGoodData.equals("Good Data");
        }

    private static long calculateMedian(JSONArray paragraphs) {
        long sum = 0;
        for (Object paragraphObj : paragraphs) {
            String paragraph = (String) paragraphObj;
            sum += paragraph.split(" ").length;
        }
        return sum / paragraphs.size();
    }

    private static long calculateWordsTotal(String wholeText) {
        String[] words = wholeText.split(" ");
        return words.length;
    }

    private static int calculateScore(int lowerThreshold, int upperThreshold, int higherThreshold, long value) {
        if (lowerThreshold <= value && value <= upperThreshold) {
            return 0;
        } else {
            return (value <= higherThreshold) ? 100 : 200;
        }
    }

    private static int calculateKeywordScore(String[] keywords, String wholeText) {
        int score = 0;
        for (String keyword : keywords) {
            if (wholeText.contains(keyword)) {
                score += 50;
            }
        }
        return score;
    }
}
