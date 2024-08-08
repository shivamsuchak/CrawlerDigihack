package v2.dataProcessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import v2.Objects.NaceCodes.NaceCodePrediction;
import v2.Objects.NaceCodes.NaceCodePredictionSet;
import v2.Objects.NaceCodes.NaceCodePredictionsList;

public class NaceCodeProcessor {

    private ConfigManager.NaceCodeConfig naceCodeConfig;
    private int maxCodes;
    private double scoreThreshold;

    public NaceCodeProcessor(ConfigManager.NaceCodeConfig naceCodeConfig) {
        this.naceCodeConfig = naceCodeConfig;
        this.maxCodes = naceCodeConfig.maxCodes;
        this.scoreThreshold = naceCodeConfig.scoreThreshold;
    }

    /**
     * Retrieves the best NACE codes from a list of NaceCodePredictionSet objects.
     *
     * @param naceCodePredictionSets A list of NaceCodePredictionSet objects.
     * @param maxCodes The maximum number of NACE codes to return.
     * @param scoreThreshold The minimum score threshold for a code to be considered.
     * @return An array of the best and unique NACE codes, limited by maxCodes.
     */
    public String[] getBestNaceCodes(List<NaceCodePredictionSet> naceCodePredictionSets, int maxCodes, double scoreThreshold) {
        PriorityQueue<NaceCodePrediction> sortedCodes = new PriorityQueue<>((a, b) -> Double.compare(b.getScore(), a.getScore()));
        Set<String> addedLabels = new HashSet<>();
        NaceCodePrediction bestAvailableCode = null;

        for (NaceCodePredictionSet set : naceCodePredictionSets) {
            for (NaceCodePredictionsList predictionsList : set.getPredictions()) {
                for (NaceCodePrediction naceCode : predictionsList.getPredictions()) {
                    if (bestAvailableCode == null || naceCode.getScore() > bestAvailableCode.getScore()) {
                        bestAvailableCode = naceCode;
                    }

                    if (!addedLabels.contains(naceCode.getLabel()) && naceCode.getScore() > scoreThreshold) {
                        sortedCodes.add(naceCode);
                        addedLabels.add(naceCode.getLabel());
                    }
                }
            }
        }

        List<String> bestCodes = new ArrayList<>();
        while (!sortedCodes.isEmpty() && bestCodes.size() < maxCodes) {
            NaceCodePrediction currentCode = sortedCodes.poll();
            bestCodes.add(currentCode.getLabel());
        }

        if (bestCodes.isEmpty() && bestAvailableCode != null) {
            bestCodes.add(bestAvailableCode.getLabel());
        }

        return bestCodes.toArray(new String[0]);
    }


    // public NaceCodePredictionsList processNaceCodes(NaceCodePredictionSet naceCodePredictionSet) {
    //     List<NaceCodePrediction> allProcessedPredictions = new ArrayList<>();

    //     for (NaceCodePredictionsList predictionsList : naceCodePredictionSet.getPredictions()) {
    //         List<NaceCodePrediction> processedPredictions = new ArrayList<>(predictionsList.getPredictions());

    //         for (String processingType : naceCodeConfig.getProcessingSequence()) {
    //             switch (processingType) {
    //                 case "LIMIT_MAX_CODES":
    //                     processedPredictions = limitMaxCodes(processedPredictions, naceCodeConfig.maxCodes);
    //                     break;
    //                 case "FILTER_BY_SCORE_THRESHOLD":
    //                     processedPredictions = filterByScoreThreshold(processedPredictions, naceCodeConfig.scoreThreshold);
    //                     break;
    //                 default:
    //                     throw new IllegalArgumentException("Unknown NaceCodeConfig processing type: " + processingType);
    //             }
    //         }

    //         // Set inputData for each NaceCodePrediction
    //         processedPredictions.forEach(prediction -> prediction.setInputData(predictionsList.getInputData()));

    //         allProcessedPredictions.addAll(processedPredictions);
    //     }

    //     // Creating a new NaceCodePredictionsList without inputData field
    //     return new NaceCodePredictionsList(allProcessedPredictions);
    // }

    // private List<NaceCodePrediction> limitMaxCodes(List<NaceCodePrediction> predictions, int max) {
    //     return predictions.stream().limit(max).collect(Collectors.toList());
    // }

    // private List<NaceCodePrediction> filterByScoreThreshold(List<NaceCodePrediction> predictions, double threshold) {
    //     return predictions.stream()
    //             .filter(prediction -> prediction.getScore() >= threshold)
    //             .collect(Collectors.toList());
    // }

}
