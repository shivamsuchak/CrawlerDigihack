package v2.dataProcessing;

import java.util.HashSet;
import java.util.Set;

/**
 * Class for evaluating and scoring the similarity between two sets of NACE codes.
 * NACE codes classify economic activities. The evaluation considers exact matches,
 * and partial matches by ignoring certain digits from the end.
 */
public class NaceCodeEvaluator {

    private static final int POINTS_FOR_EXACT_MATCH = 10;
    private static final int POINTS_FOR_MATCH_IGNORE_LAST_DIGIT = 3;
    private static final int POINTS_FOR_MATCH_IGNORE_LAST_TWO_DIGITS = 1;
    private static final int POINTS_FOR_MISMATCH = -10;

    private int exactMatchCount;
    private int matchIgnoringLastDigitCount;
    private int matchIgnoringLastTwoDigitsCount;
    private int totalScore;
    private int totalComparisons = 0;

    /**
     * Compares two arrays of NACE codes and updates the match counts and total score.
     *
     * @param validationSet Array of valid NACE codes.
     * @param testSet Array of test NACE codes.
     */
    public void compareNaceCodes(String[] validationSet, String[] testSet) {
        Set<String> usedCodes = new HashSet<>();
        evaluateMatches(testSet, validationSet, usedCodes);
        penalizeUnusedValidationCodes(validationSet, usedCodes);
        totalComparisons ++;
    }

    private void evaluateMatches(String[] testSet, String[] validationSet, Set<String> usedCodes) {
        for (String testCode : testSet) {
            boolean matchFound = false;
            for (String validCode : validationSet) {
                if (!usedCodes.contains(validCode)) {
                    matchFound = checkAndUpdateMatches(testCode, validCode, usedCodes);
                    if (matchFound) break;
                }
            }
            if (!matchFound) updateScore(POINTS_FOR_MISMATCH);
        }
    }

    private boolean checkAndUpdateMatches(String testCode, String validCode, Set<String> usedCodes) {
        if (validCode.equals(testCode)) {
            updateMatchCounts(POINTS_FOR_EXACT_MATCH, usedCodes, validCode);
            return true;
        } else if (validCode.startsWith(testCode.substring(0, Math.min(3, testCode.length())))) {
            updateMatchCounts(POINTS_FOR_MATCH_IGNORE_LAST_DIGIT, usedCodes, validCode);
            return true;
        } else if (validCode.startsWith(testCode.substring(0, Math.min(2, testCode.length())))) {
            updateMatchCounts(POINTS_FOR_MATCH_IGNORE_LAST_TWO_DIGITS, usedCodes, validCode);
            return true;
        }
        return false;
    }

    private void updateMatchCounts(int points, Set<String> usedCodes, String validCode) {
        updateScore(points);
        usedCodes.add(validCode);
        incrementMatchCount(points);
    }

    private void updateScore(int points) {
        totalScore += points;
    }

    private void incrementMatchCount(int points) {
        switch (points) {
            case POINTS_FOR_EXACT_MATCH:
                exactMatchCount++;
                break;
            case POINTS_FOR_MATCH_IGNORE_LAST_DIGIT:
                matchIgnoringLastDigitCount++;
                break;
            case POINTS_FOR_MATCH_IGNORE_LAST_TWO_DIGITS:
                matchIgnoringLastTwoDigitsCount++;
                break;
            default:
        }
    }

    private void penalizeUnusedValidationCodes(String[] validationSet, Set<String> usedCodes) {
        for (String validCode : validationSet) {
            if (!usedCodes.contains(validCode)) {
                updateScore(POINTS_FOR_MISMATCH);
            }
        }
    }

    /**
     * Prints an overview of the NACE code evaluation results.
     */
    public void printResultsOverview() {
        System.out.println("Nace Code Evaluation Results:");
        System.out.println("Total Exact Matches: " + exactMatchCount);
        System.out.println("Total Matches Ignoring Last Digit: " + matchIgnoringLastDigitCount);
        System.out.println("Total Matches Ignoring Last Two Digits: " + matchIgnoringLastTwoDigitsCount);
        System.out.println("Total Score: " + totalScore);
        System.out.println("Total Comparisons: " + totalComparisons);
    }

    // Getters for various match counts and total score
    // Getters for various match counts and total score
    public int getExactMatchCount() {
        return exactMatchCount;
    }

    public int getMatchIgnoringLastDigitCount() {
        return matchIgnoringLastDigitCount;
    }

    public int getMatchIgnoringLastTwoDigitsCount() {
        return matchIgnoringLastTwoDigitsCount;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getTotalComparisons() {
        return totalComparisons;
    }
}


