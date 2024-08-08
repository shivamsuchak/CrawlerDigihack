package v2.crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class ExceptionTracker {
    private Map<Integer, Set<String>> httpExceptions;
    private Map<String, Set<String>> ioExceptions;

    public ExceptionTracker() {
        this.httpExceptions = new HashMap<>();
        this.ioExceptions = new HashMap<>();
    }

    public void addHttpException(int statusCode, String url) {
        this.httpExceptions.computeIfAbsent(statusCode, k -> new HashSet<>()).add(url);
    }

    public void addIoException(String message, String url) {
        this.ioExceptions.computeIfAbsent(message, k -> new HashSet<>()).add(url);
    }

    public JSONObject generateJsonReport() {
        JSONObject report = new JSONObject();

        JSONArray httpErrors = new JSONArray();
        this.httpExceptions.forEach((statusCode, urls) -> {
            JSONObject error = new JSONObject();
            error.put("status code", statusCode);
            error.put("occurrence", urls.size());
            error.put("links", new JSONArray(urls));
            httpErrors.put(error);
        });
        report.put("HTTP Exceptions", httpErrors);

        JSONArray ioErrors = new JSONArray();
        this.ioExceptions.forEach((message, urls) -> {
            JSONObject error = new JSONObject();
            error.put("error message", message);
            error.put("occurrence", urls.size());
            error.put("links", new JSONArray(urls));
            ioErrors.put(error);
        });
        report.put("IO Exceptions", ioErrors);

        return report;
    }

    public void writeReportToJsonFile(String filePath) {
        JSONObject report = generateJsonReport();
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(report.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
