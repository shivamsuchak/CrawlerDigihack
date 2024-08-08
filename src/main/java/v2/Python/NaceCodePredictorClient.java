package v2.Python;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import v2.Objects.NaceCodes.NaceCodePrediction;
import v2.Objects.NaceCodes.NaceCodePredictionsList;

public class NaceCodePredictorClient {

    private static final String SERVICE_URL = "http://localhost:5000/predict";
    private static final String CONTENT_TYPE = "application/json; utf-8";
    private static final String ACCEPT = "application/json";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static NaceCodePredictionsList predictNaceCode(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return new NaceCodePredictionsList(""); // Return an empty NaceCodePredictionsList
        }

        ObjectMapper objectMapper = new ObjectMapper(); // Ensure you have an ObjectMapper instance
        try {
            HttpURLConnection conn = createConnection(SERVICE_URL); // Replace with your actual method to create a connection
            sendRequest(conn, inputText); // Replace with your actual method to send a request
            String jsonResponse = getResponse(conn); // Replace with your actual method to get a response
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode predictionsNode = rootNode.path("predictions");

            List<NaceCodePrediction> predictions = objectMapper.readValue(
                predictionsNode.toString(), new TypeReference<List<NaceCodePrediction>>() {});

            NaceCodePredictionsList predictionsList = new NaceCodePredictionsList(inputText);
            predictions.forEach(predictionsList::addPrediction);

            return predictionsList;
        } catch (Exception e) {
            e.printStackTrace();
            return new NaceCodePredictionsList(inputText); // Return an empty NaceCodePredictionsList in case of an error
        }
    }

    private static HttpURLConnection createConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", CONTENT_TYPE);
        conn.setRequestProperty("Accept", ACCEPT);
        conn.setDoOutput(true);
        return conn;
    }

    private static void sendRequest(HttpURLConnection conn, String inputText) throws Exception {
        JsonNode jsonNode = objectMapper.createObjectNode().put("text", inputText);
        String jsonInputString = objectMapper.writeValueAsString(jsonNode);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }


    private static String getResponse(HttpURLConnection conn) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } finally {
            conn.disconnect();
        }
        return response.toString();
    }
}
