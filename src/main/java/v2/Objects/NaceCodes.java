package v2.Objects;

import java.util.ArrayList;
import java.util.List;

public class NaceCodes {

    public static class NaceCodePrediction {
        private String label;
        private double score;
        private String inputData;

        // Existing constructor and methods

        @Override
        public String toString() {
            return "{ \"label\": \"" + label + "\", \"score\": " + score + " }";
        }

        public double getScore() {
            return score;
        }

        public String getLabel() {
            return label;
        }

        public void setInputData(String inputData) {
            this.inputData = inputData;
        }

        public String getInputData() {
            return inputData;
        }
    }

    public static class NaceCodePredictionsList {
        private List<NaceCodePrediction> predictions;
        private String inputData;
        private String inputDataType; // New field for input data type

        public NaceCodePredictionsList(String inputData, String inputDataType) {
            this.predictions = new ArrayList<>();
            this.inputData = inputData;
            this.inputDataType = inputDataType; // Initialize the input data type
        }

        public NaceCodePredictionsList(String inputData) {
            this.predictions = new ArrayList<>();
            this.inputData = inputData;
        }


        public void addPrediction(NaceCodePrediction prediction) {
            predictions.add(prediction);
        }

        public List<NaceCodePrediction> getPredictions() {
            return predictions;
        }

        public String getInputData() {
            return inputData;
        }

        public void setInputData(String inputData) {
            this.inputData = inputData;
        }

        public String getInputDataType() {
            return inputDataType;
        }

        public void setInputDataType(String inputDataType) {
            this.inputDataType = inputDataType;
        }

        @Override
        public String toString() {
            return "Input Data: " + inputData + ", Input Data Type: " + inputDataType + ", NaceCodePredictions: " + predictions.toString();
        }

        public void addInputData(String text) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'addInputData'");
        }
    }


    public static class NaceCodePredictionSet {
        private List<NaceCodePredictionsList> predictions;

        public NaceCodePredictionSet() {
            this.predictions = new ArrayList<>();
        }

        public void addPrediction(NaceCodePredictionsList naceCodePredictionsList) {
            predictions.add(naceCodePredictionsList);
        }

        public List<NaceCodePredictionsList> getPredictions() {
            return predictions;
        }
    }
}
