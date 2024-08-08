package v2.crawler.ErrorTracking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorTracker {
    private static ErrorTracker instance;
    private List<ErrorDetail> errorList;

    private ErrorTracker() {
        errorList = new ArrayList<>();
    }

    public static synchronized ErrorTracker getInstance() {
        if (instance == null) {
            instance = new ErrorTracker();
        }
        return instance;
    }

        /**
     * Generates a summary of the current errors.
     *
     * @return A string representation of the error summary.
     */
    public String generateErrorSummary() {
        Map<String, Integer> errorCountByType = new HashMap<>();
        StringBuilder summary = new StringBuilder();

        for (ErrorDetail error : errorList) {
            String key = error.getErrorType();
            errorCountByType.put(key, errorCountByType.getOrDefault(key, 0) + 1);
        }

        summary.append("Error Summary:\n");
        for (Map.Entry<String, Integer> entry : errorCountByType.entrySet()) {
            summary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return summary.toString();
    }

    /**
     * Provides a detailed overview of all tracked errors.
     *
     * @return A string representation of all errors.
     */
    public String getDetailedErrorOverview() {
        StringBuilder details = new StringBuilder();
        details.append("Detailed Error Overview:\n");

        for (ErrorDetail error : errorList) {
            details.append(error.toString()).append("\n\n");
        }

        return details.toString();
    }

        /**
     * Writes the detailed error overview and summary to a file.
     *
     * @param filePath The path of the file to write to.
     */
    public void writeErrorsToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(getDetailedErrorOverview());
            writer.write("\n");
            writer.write(generateErrorSummary());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            // Handle exception or log error as needed
        }
    }

    public void addError(ErrorDetail error) {
        errorList.add(error);
    }

    public List<ErrorDetail> getErrorList() {
        return errorList;
    }
}
