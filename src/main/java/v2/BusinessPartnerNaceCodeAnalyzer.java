package v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.jsoup.nodes.Document;

import v2.Objects.BusinessPartner;
import v2.Objects.NaceCodes.NaceCodePredictionSet;
import v2.Objects.NaceCodes.NaceCodePredictionsList;
import v2.Python.NaceCodePredictorClient;
import v2.crawler.Crawler;
import v2.crawler.ErrorTracking.ErrorTracker;
import v2.dataProcessing.ConfigManager;
import v2.dataProcessing.ConfigManager.NaceCodeConfig;
import v2.dataProcessing.FileManager;
import v2.dataProcessing.HtmlProcessor;
import v2.dataProcessing.NaceCodeEvaluator;
import v2.dataProcessing.NaceCodeProcessor;
import v2.dataProcessing.TextProcessor;

public class BusinessPartnerNaceCodeAnalyzer {

    private final static String BP_JSON_PATH = "data/osapiens_BP.json";
    private final static String ERROR_LOG_PATH = "data/Output/errorLog/error.log";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        List<BusinessPartner> businessPartners = chooseBusinessPartnerAction(scanner);
        websiteCrawling(scanner, businessPartners);

        ConfigManager configManager = new ConfigManager();

        // TODO: handle keywords
        HtmlProcessor htmlProcessor = new HtmlProcessor(configManager.getHtmlConfig());
        TextProcessor textProcessor = new TextProcessor(configManager.getTextConfig());

        for (BusinessPartner businessPartner : businessPartners) {
            List<Document> documents = FileManager.loadDocumentsFromFile("data/Output/CrawledDocuments/" + businessPartner.getBusinessPartnerKey() + ".json");
            htmlProcessor.processHtml(documents);
            for (Document doc : documents) {
                // TODO: add a tag to every processed text (for example: "summary" or "text")
                List<String> processedText = textProcessor.processTexts(doc);
                if (processedText != null && !processedText.isEmpty())
                    businessPartner.addProcessedData(processedText);
            }
        }

        for (BusinessPartner businessPartner : businessPartners) {
            List<List<String>> processedData = businessPartner.getProcessedData();
            for (List<String> texts : processedData) {
                NaceCodePredictionSet predictionSet = new NaceCodePredictionSet();
                for (String text : texts) {
                    if (text != null && !text.isEmpty()) {
                        NaceCodePredictionsList predictionsList = NaceCodePredictorClient.predictNaceCode(text);
                        predictionsList.setInputData(text);
                        predictionSet.addPrediction(predictionsList);
                    }
                }
                if (!predictionSet.getPredictions().isEmpty()) {
                    businessPartner.addNaceCodePredictionSet(predictionSet);
                    System.out.println("predicted NACE codes for BP " + businessPartner.getBusinessPartnerKey());
                }
            }
        }

        NaceCodeConfig naceCodeConfig = configManager.getNaceCodeConfig();
        NaceCodeProcessor naceCodeProcessor = new NaceCodeProcessor(naceCodeConfig);
        NaceCodeEvaluator naceCodeEvaluation = new NaceCodeEvaluator();

        for (BusinessPartner businessPartner : businessPartners) {

            String[] bestNaceCodes = naceCodeProcessor.getBestNaceCodes(businessPartner.getNaceCodePredictionSets(), naceCodeConfig.maxCodes, naceCodeConfig.scoreThreshold);
            businessPartner.setNaceCodes(bestNaceCodes);

            naceCodeEvaluation.compareNaceCodes(businessPartner.getDunsAndBradstreetCodes(), businessPartner.getNaceCodes());
        }

        naceCodeEvaluation.printResultsOverview();

        // Save results
        BusinessPartner.saveBusinessPartnersAsJson(businessPartners, "data/Output/SavedBusinessPartners/");

        scanner.close();
        System.exit(0);
    }

    /**
     * Asks the user if they want to crawl websites and performs the crawling if yes, also saves the BP in JSON format with the crawled data.
     *
     * @param scanner          Scanner for reading user input.
     * @param businessPartners List of BusinessPartner objects to crawl.
     * @param errorTracker     ErrorTracker instance for logging errors.
     */
    private static void websiteCrawling(Scanner scanner, List<BusinessPartner> businessPartners) {
        int crawlChoice;

        do {
            System.out.println("Do you want to crawl websites? \n1: Yes \n2: No");
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter 1 or 2.");
                scanner.next(); // Clear the invalid input
            }
            crawlChoice = scanner.nextInt();

            switch (crawlChoice) {
                case 1:
                Crawler crawler = new Crawler();
                for (BusinessPartner businessPartner : businessPartners) {
                    System.out.println("Crawling website: " + businessPartner.getWebsite());
                    CompletableFuture<List<Document>> futureDocuments = crawler.crawlFullPage(businessPartner.getWebsite());

                    try {
                        List<Document> crawledData = futureDocuments.get();
                        if (crawledData != null && !crawledData.isEmpty()) {
                            FileManager.saveDocumentsToFile(crawledData, "data/Output/CrawledDocuments/" + businessPartner.getBusinessPartnerKey() + ".json");
                            FileManager.saveHtmlFromDocsToJson(crawledData, "data/Output/CrawledHtml/" + businessPartner.getBusinessPartnerKey() + ".json");
                        } else {
                            System.err.println("No data crawled for: " + businessPartner.getWebsite());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    ErrorTracker.getInstance().writeErrorsToFile(ERROR_LOG_PATH);
                }
                break;

                case 2:
                    // No action needed if the user selects no
                    break;

                default:
                    System.out.println("Invalid selection. Please choose 1 or 2.");
                    crawlChoice = 0; // Reset to ensure the loop continues
                    break;
            }
        } while (crawlChoice != 1 && crawlChoice != 2);
    }

    /**
     * Prompts the user to choose an action regarding Business Partners and processes the choice.
     *
     * @param scanner Scanner for reading user input.
     * @return A list of BusinessPartner objects based on the user's choice.
     */
    private static List<BusinessPartner> chooseBusinessPartnerAction(Scanner scanner) {
        List<BusinessPartner> businessPartners = new ArrayList<>();
        int userChoice;

        do {
            System.out.println("Choose action: \n1 for loading already processed BusinessPartners\n2 for Initialize new BusinessPartners");
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter 1 or 2.");
                scanner.next(); // Clear the invalid input
            }
            userChoice = scanner.nextInt();

            switch (userChoice) {
                case 1:
                    // Load existing BusinessPartners from JSON
                    System.out.println("Enter the path to the json file:");
                    String path = scanner.next();
                    businessPartners = BusinessPartner.loadAllBusinessPartnersFromJson(path);
                    break;

                case 2:
                    // Initialize new BusinessPartners from JSON
                    System.out.println("Enter the number of business partners to initialize from: " + BP_JSON_PATH);
                    int numberOfBPs = scanner.nextInt();
                    businessPartners = BusinessPartner.initBpFromJson(BP_JSON_PATH, numberOfBPs);
                    break;

                default:
                    System.out.println("Invalid action selected. Please choose 1 or 2.");
                    break;
            }
        } while (userChoice != 1 && userChoice != 2);
        return businessPartners;
    }
}
