package v2.dataProcessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FileManager {

    private static final Gson gson = new Gson();

    public static void saveDocumentsToFile(List<Document> crawledData, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (Document doc : crawledData) {
                if (doc != null) {
                    // Serialize each document to JSON including metadata
                    String json = serializeDocumentToJson(doc);
                    writer.write(json);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Document> loadDocumentsFromFile(String path) {
        List<Document> documents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Deserialize each line as a separate document
                Document doc = deserializeJsonToDocument(line);
                if (doc != null) {
                    documents.add(doc);
                }
            }
        } catch (IOException e) {
            System.out.println("No Crawled data for:" + path);
        }
        return documents;
    }

    public static String serializeDocumentToJson(Document doc) {
        JsonObject jsonDoc = new JsonObject();
        jsonDoc.addProperty("html", doc.html());
        jsonDoc.addProperty("baseUri", doc.baseUri());
        jsonDoc.addProperty("charset", doc.outputSettings().charset().name());
        jsonDoc.addProperty("escapeMode", doc.outputSettings().escapeMode().name());
        return gson.toJson(jsonDoc);
    }

    public static Document deserializeJsonToDocument(String json) {
        JsonObject jsonDoc = gson.fromJson(json, JsonObject.class);
        Document doc = Jsoup.parse(jsonDoc.get("html").getAsString());
        doc.setBaseUri(jsonDoc.get("baseUri").getAsString());
        doc.outputSettings().charset(jsonDoc.get("charset").getAsString());
        doc.outputSettings().escapeMode(Entities.EscapeMode.valueOf(jsonDoc.get("escapeMode").getAsString()));
        return doc;
    }

    public static void saveHtmlFromDocsToJson(List<Document> documents, String jsonFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFilePath))) {
            JsonObject json = new JsonObject();
            JsonArray documentArray = new JsonArray();

            // Iterate through documents and add them to the JSON array
            for (Document doc : documents) {
                if (doc != null) {
                    JsonObject documentJson = new JsonObject();
                    documentJson.addProperty("url", doc.baseUri());
                    documentJson.addProperty("html", doc.html());
                    documentArray.add(documentJson);
                }
            }

            json.add("documents", documentArray);

            // Write the JSON object to the file
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
