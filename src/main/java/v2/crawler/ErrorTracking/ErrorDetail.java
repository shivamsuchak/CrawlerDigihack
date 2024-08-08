package v2.crawler.ErrorTracking;

public class ErrorDetail {
    private String errorSource;
    private String errorType;
    private String url;
    private String message;
    private Integer statusCode;

    public ErrorDetail(String errorSource, String errorType, String url, String message, Integer statusCode) {
        this.errorSource = errorSource;
        this.errorType = errorType;
        this.url = url;
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "Error Source: " + errorSource +
               ", Error Type: " + errorType +
               ", URL: " + url +
               ", Message: " + message +
               (statusCode != null ? ", Status Code: " + statusCode : "");
    }

    public String getErrorType() {
        return errorType;
    }
}
