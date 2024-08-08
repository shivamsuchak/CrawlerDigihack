package v2.Objects;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import v2.Objects.NaceCodes.NaceCodePredictionSet;

public class BusinessPartner {

    private static final String DunsAndBradstreetCodesPath = "data/duns_and_bradstreet_BP.json";

    private String businessPartnerKey;
    private String website;
    // Crawled data as Jsoup Documents

    private List<List<String>> processedData;
    private List<NaceCodePredictionSet> naceCodePredictionSets;
    private String[] dunsAndBradstreetCodes;
    private String[] naceCodes;

    public BusinessPartner() {
        this.naceCodePredictionSets = new ArrayList<>();
        this.processedData = new ArrayList<>();
        this.dunsAndBradstreetCodes = new String[0];
        this.naceCodes = new String[0];
    }


    public BusinessPartner(String businessPartnerKey, String website) {
        this.businessPartnerKey = businessPartnerKey;
        this.website = website;
        this.naceCodePredictionSets = new ArrayList<>();
        this.processedData = new ArrayList<>();
        this.dunsAndBradstreetCodes = AddDunAndBradStreetCodes(DunsAndBradstreetCodesPath);
        this.naceCodes = new String[0];
    }

    /**
     * Loads BusinessPartner objects from a JSON file.
     *
     * @param filePath The path to the JSON file containing BusinessPartner data.
     * @return A list of BusinessPartner objects.
     */
    public static List<BusinessPartner> initBpFromJson(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> jsonList = new ArrayList<>();
        try {
            jsonList = mapper.readValue(new File(filePath), new TypeReference<List<Map<String, Object>>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonList.stream().map(jsonMap -> {
            String key = (String) jsonMap.get("key");
            String website = (String) jsonMap.get("website");

            return new BusinessPartner(key, website);
        }).collect(Collectors.toList());
    }

    /**
     * Loads a specified number of BusinessPartner objects from a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @param maxNumberOfBusinessPartners The maximum number of BusinessPartner objects to load.
     * @return A list of BusinessPartner objects.
     */
    public static List<BusinessPartner> initBpFromJson(String filePath, int maxNumberOfBusinessPartners) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> jsonList = new ArrayList<>();
        try {
            jsonList = mapper.readValue(new File(filePath), new TypeReference<List<Map<String, Object>>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonList.stream()
                       .limit(maxNumberOfBusinessPartners)
                       .map(jsonMap -> {
                           String key = (String) jsonMap.get("key");
                           String website = (String) jsonMap.get("website");

                           return new BusinessPartner(key, website);
                       }).collect(Collectors.toList());
    }

    /**
     * Reads a single BusinessPartner from a JSON file by its businessPartnerKey.
     *
     * @param fileName The name of the file to read from.
     * @param businessPartnerKey The key of the BusinessPartner to read.
     * @return The BusinessPartner object if found, otherwise null.
     */
    public static BusinessPartner loadSingleBusinessPartnerFromJson(String fileName, String businessPartnerKey) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(fileName);

        try {
            List<Map<String, Object>> partnersData = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>(){});
            for (Map<String, Object> dataMap : partnersData) {
                if (dataMap.get("businessPartnerKey").equals(businessPartnerKey)) {
                    BusinessPartner partner = mapper.convertValue(dataMap, BusinessPartner.class);
                    return partner;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if the BusinessPartner is not found
    }


    /**
     * Loads all BusinessPartner objects from a JSON file.
     *
     * @param fileName The name of the file to read from.
     * @return A list of BusinessPartner objects.
     */
    public static List<BusinessPartner> loadAllBusinessPartnersFromJson(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(fileName);

        try {
            List<Map<String, Object>> partnersData = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>(){});
            List<BusinessPartner> bp = partnersData.stream()
                            .map(dataMap -> mapper.convertValue(dataMap, BusinessPartner.class))
                            .collect(Collectors.toList());
            return bp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // Return an empty list if there's an error
    }

    public static List<BusinessPartner> loadAllBusinessPartnersFromFolder(String folderPath) {
        ObjectMapper mapper = new ObjectMapper();
        // Configure ObjectMapper to ignore unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        File folder = new File(folderPath);
        List<BusinessPartner> businessPartners = new ArrayList<>();

        // Filter JSON files from the folder
        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                try {
                    // Read each file and convert it to BusinessPartner object
                    BusinessPartner partner = mapper.readValue(file, BusinessPartner.class);
                    businessPartners.add(partner);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return businessPartners;
    }


    /**
     * Saves a list of BusinessPartner objects as a JSON file, including data from transient fields.
     *
     * @param businessPartners The list of BusinessPartner objects to save.
     * @param fileName The name of the file to save the JSON data to.
     */
    public static void saveBusinessPartnersAsJson(List<BusinessPartner> businessPartners, String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        // Ensure ObjectMapper does not change the order
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        List<Map<String, Object>> partnersData = new ArrayList<>();
        for (BusinessPartner partner : businessPartners) {
            LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("businessPartnerKey", partner.getBusinessPartnerKey());
            dataMap.put("website", partner.getWebsite());
            dataMap.put("dunsAndBradstreetCodes", partner.getDunsAndBradstreetCodes());
            dataMap.put("naceCodes", partner.getNaceCodes());
            dataMap.put("naceCodePredictions", partner.getNaceCodePredictionSets());
            dataMap.put("processedData", partner.getProcessedData());
            partnersData.add(dataMap);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String fullFileName = fileName + timestamp + ".json";

        try {
            mapper.writeValue(new File(fullFileName), partnersData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a single BusinessPartner object to a JSON file. Appends to the file if it exists, or creates a new one if it does not.
     *
     * @param businessPartner The BusinessPartner object to save.
     * @param fileName The name of the file to save the JSON data to.
     */
    public static void saveBusinessPartnerAsJson(BusinessPartner businessPartner, String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        // Ensure ObjectMapper does not change the order
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        List<Map<String, Object>> partnersData = new ArrayList<>();
        File file = new File(fileName);

        // Check if the file exists and read the existing BusinessPartners
        if (file.exists()) {
            try {
                List<Map<String, Object>> existingData = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>(){});
                partnersData.addAll(existingData);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the error as appropriate
            }
        }

        // Add the new BusinessPartner
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("businessPartnerKey", businessPartner.getBusinessPartnerKey());
        dataMap.put("website", businessPartner.getWebsite());
        dataMap.put("dunsAndBradstreetCodes", businessPartner.getDunsAndBradstreetCodes());
        dataMap.put("naceCodes", businessPartner.getNaceCodes());
        dataMap.put("naceCodePredictions", businessPartner.getNaceCodePredictionSets());
        dataMap.put("processedData", businessPartner.getProcessedData());
        // dataMap.put("crawledData", businessPartner.getCrawledData());
        partnersData.add(dataMap);

        // Write to the file
        try {
            mapper.writeValue(file, partnersData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a BusinessPartner entry in a JSON file.
     *
     * @param fileName The name of the file to read from and write to.
     * @param businessPartnerKey The key of the BusinessPartner to update.
     * @param updatedPartner The BusinessPartner object with updated values.
     * @return true if update is successful, otherwise false.
     */
    public static boolean updateBusinessPartnerInJson(String fileName, String businessPartnerKey, BusinessPartner updatedPartner) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(fileName);

        try {
            List<Map<String, Object>> partnersData = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
            boolean isUpdated = false;

            for (int i = 0; i < partnersData.size(); i++) {
                Map<String, Object> dataMap = partnersData.get(i);
                if (dataMap.get("businessPartnerKey").equals(businessPartnerKey)) {
                    partnersData.set(i, mapper.convertValue(updatedPartner, Map.class));
                    isUpdated = true;
                    break;
                }
            }

            if (isUpdated) {
                mapper.writeValue(file, partnersData); // Write the updated list back to the file
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Return false if the update is not successful
    }

    /**
     * Adds Dun & Bradstreet industry codes to the BusinessPartner based on a given file.
     *
     * @param filePath The path to the JSON file containing the Dun & Bradstreet codes.
     * @return An array of Dun & Bradstreet codes.
     */
    public String[] AddDunAndBradStreetCodes(String filePath) {
        JSONParser parser = new JSONParser();

        try {
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(filePath));

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                String key = (String) jsonObject.get("key");

                if (businessPartnerKey != null && businessPartnerKey.equals(key)) {
                    JSONArray dunsCodes = (JSONArray) jsonObject.get("dunsIndustryCodeList");

                    if (dunsCodes != null) {
                        dunsAndBradstreetCodes = new String[dunsCodes.size()];
                        for (int i = 0; i < dunsCodes.size(); i++) {
                            String code = (String) dunsCodes.get(i);
                            // Remove the period from the code
                            dunsAndBradstreetCodes[i] = code.replace(".", "");
                        }
                        return dunsAndBradstreetCodes; // Return the array of formatted codes
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        // Return an empty array if no codes are found or an exception occurs
        return new String[0];
    }


    // Getters and Setters
    public String getBusinessPartnerKey() {
        return businessPartnerKey;
    }

    public void setBusinessPartnerKey(String businessPartnerKey) {
        this.businessPartnerKey = businessPartnerKey;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<NaceCodePredictionSet> getNaceCodePredictionSets() {
        return naceCodePredictionSets;
    }

    public void addNaceCodePredictionSet(NaceCodePredictionSet naceCodePredictionSet) {
        this.naceCodePredictionSets.add(naceCodePredictionSet);
    }

    public List<List<String>> getProcessedData() {
        return processedData;
    }

    public void setProcessedData(List<List<String>> processedData) {
        this.processedData = processedData;
    }

    public void addProcessedData(List<String> processedData) {
        this.processedData.add(processedData);
    }

    public String[] getDunsAndBradstreetCodes() {
        return dunsAndBradstreetCodes;
    }

    public String[] getNaceCodes() {
        return naceCodes;
    }

    public void setNaceCodes(String[] naceCodes) {
        this.naceCodes = naceCodes;
    }
}
