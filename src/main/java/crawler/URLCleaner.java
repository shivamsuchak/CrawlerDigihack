package crawler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class URLCleaner {
    /**
     * Processes a JSONArray of JSON objects containing URL data.
     * Cleans each URL by handling different cases such as the presence of commas or at-signs.
     * Returns a new JSONArray with the cleaned URL data alongside other relevant information.
     */
    static JSONArray cleanURL(JSONArray jsonArray) {
        JSONArray cleaned = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject element = jsonArray.getJSONObject(i);
                String url = element.optString("website", "");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", element.optString("key", ""));
                jsonObject.put("name", element.optString("name", ""));
                jsonObject.put("industryCodeList", element.getJSONArray("industryCodeList").getString(0));

                if (url.contains(",")) {
                    // Case 1: Split the URL by "," and take the first part
                    String parts = url.split(",")[0];
                    jsonObject.put("website", cleanPath(parts));
                } else if (url.contains("@")) {
                    // Case 2: Split the URL by "@" and take the second part
                    String parts = url.split("@")[1];
                    jsonObject.put("website", cleanPath("https://www." + parts));
                } else {
                    jsonObject.put("website", cleanPath(url));
                }

                cleaned.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cleaned;
    }

    private static String cleanPath(String url) {
        try {
            URL parsedUrl = new URL(url);
            return parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
        } catch (MalformedURLException e) {
            // Handle invalid URLs
            return url;
        }
    }
}
