    package v2.dataProcessing;

    import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class ConfigManager {

        private HtmlConfig htmlConfig;
        private TextConfig textConfig;
        private NaceCodeConfig naceCodeConfig;

        public ConfigManager() {
            createDefaultConfig();
        }

        public void createDefaultConfig() {
            this.htmlConfig = new HtmlConfig();
            this.htmlConfig.rankSubPages = false;
            this.htmlConfig.rankSubPagesBy = new HashMap<>();
            this.htmlConfig.maxSubPages = 5;
            this.htmlConfig.minPriorityThreshold = 3;
            this.htmlConfig.shouldConsiderUrlDepth = true;
            this.htmlConfig.ignoreTags = Arrays.asList(  "nav", "header", "footer");
            this.htmlConfig.processingSequence = Arrays.asList(
                "rankSubPages",
                "maxSubPages",
                "minPriorityThreshold",
                "shouldConsiderUrlDepth",
                "ignoreTags"
            );

            this.textConfig = new TextConfig();
            this.textConfig.removeShortParagraphs = 100;
            this.textConfig.splitParagraphsAt = 512;
            this.textConfig.removeParagraphsWithKeywords = Arrays.asList(  "datenschutz",
            "cookies",
            "cookie-richtlinie",
            "nutzungsbedingungen",
            "agb",
            "impressum",
            "datenschutzerklärung",
            "cookie-einstellungen",
            "datenschutzeinstellungen",
            "datenschutzrichtlinien",
            "java script",
            // javascript related keywords
            "javascript",
            "js",
            "ecmascript",
            "script");
            this.textConfig.removeDuplicatedParagraphs = true;
            this.textConfig.summarizeText = true;
            this.textConfig.processingSequence = Arrays.asList(
                "splitParagraphsAt",
                "removeShortParagraphs",
                "removeParagraphsWithKeywords",
                "removeDuplicatedParagraphs"
            );

            // Initialize NaceCodeConfig with default values and sequence
            this.naceCodeConfig = new NaceCodeConfig();
            this.naceCodeConfig.maxCodes = 3;
            this.naceCodeConfig.scoreThreshold = 0.7;

            // TODO: create dynamic
            this.naceCodeConfig.processingSequence = Arrays.asList("TYPE_1", "TYPE_2");
        }

        // Getters for configurations
        public HtmlConfig getHtmlConfig() {
            return htmlConfig;
        }

        public TextConfig getTextConfig() {
            return textConfig;
        }

        public NaceCodeConfig getNaceCodeConfig() {
            return naceCodeConfig;
        }

        // Inner class for Document configuration
        public class HtmlConfig {
            public List<String> ignoreTags;
            public boolean rankSubPages;
            public Map<Integer, List<String>> rankSubPagesBy;
            public int maxSubPages;
            public int minPriorityThreshold;
            public boolean shouldConsiderUrlDepth;
            public List<String> processingSequence;

            public List<String> getProcessingSequence() {
                return processingSequence;
            }
        }

        // Inner class for HTML configuration
        public class TextConfig {
            public List<String> ignoreTags;
            public boolean createTextOutOfParagraphs;
            public int removeShortParagraphs;
            public String splitParam;
            public int splitParagraphsAt;
            public List<String> removeParagraphsWithKeywords;
            public boolean removeDuplicatedParagraphs;
            public List<String> processingSequence;
            public boolean summarizeText;
            public List<String> getProcessingSequence() {
                return processingSequence;
            }
        }

        // Inner class for NACE Code configuration
        public class NaceCodeConfig {
            public int maxCodes;
            public double scoreThreshold;
            public List<String> processingSequence;
            public List<String> getProcessingSequence() {
                return processingSequence;
            }
        }
    }
