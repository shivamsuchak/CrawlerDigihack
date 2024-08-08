import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import v2.dataProcessing.NaceCodeEvaluator;

class NaceCodeEvaluatorTest {

    private NaceCodeEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new NaceCodeEvaluator();
    }

    @Test
    void testCompareNaceCodesWithExactMatches() {
        String[] validationSet = {"1234", "5678", "9101"};
        String[] testSet = {"1234", "5678", "9101"};
        evaluator.compareNaceCodes(validationSet, testSet);

        assertEquals(3, evaluator.getExactMatchCount());
        assertEquals(0, evaluator.getMatchIgnoringLastDigitCount());
        assertEquals(0, evaluator.getMatchIgnoringLastTwoDigitsCount());
        assertEquals(30, evaluator.getTotalScore()); // 3 exact matches
    }

    @Test
    void testCompareNaceCodesWithPartialAndNoMatches() {
        String[] validationSet = {"1234", "5678", "9101"};
        String[] testSet = {"123", "56780", "0000"};
        evaluator.compareNaceCodes(validationSet, testSet);

        assertEquals(0, evaluator.getExactMatchCount());
        assertEquals(2, evaluator.getMatchIgnoringLastDigitCount()); // "123", "56780"
        assertEquals(0, evaluator.getMatchIgnoringLastTwoDigitsCount());
        assertEquals(5, evaluator.getTotalScore()); // 2 partial matches and 1 mismatch
    }

    @Test
    void testCompareNaceCodesWithNoMatches() {
        String[] validationSet = {"1234", "5678"};
        String[] testSet = {"0000", "1111"};
        evaluator.compareNaceCodes(validationSet, testSet);

        assertEquals(0, evaluator.getExactMatchCount());
        assertEquals(0, evaluator.getMatchIgnoringLastDigitCount());
        assertEquals(0, evaluator.getMatchIgnoringLastTwoDigitsCount());
        assertEquals(-4, evaluator.getTotalScore());
    }

}

