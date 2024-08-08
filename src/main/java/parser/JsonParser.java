package parser;

import java.io.BufferedReader;
import java.io.FileReader;

public class JsonParser {
    public static String getJSONFromFile(String filename) {
        StringBuilder jsonText = new StringBuilder();
        try {		
            BufferedReader bufferedReader = 
                          new BufferedReader(new FileReader(filename));
        
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonText.append(line).append("\n");
            }
        
            bufferedReader.close();
        
        } catch (Exception e) {
            System.err.println("Error reading the JSON file");
        }
    
        return jsonText.toString();
    }
}