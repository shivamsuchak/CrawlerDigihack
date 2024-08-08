package crawler;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExceptionTracker {
    private List<String> socketExceptionsList = new ArrayList<>();
    private List<String> socketTimeoutList = new ArrayList<>();
    private List<String> httpStatusExceptionsList = new ArrayList<>();
    private List<String> unknownHostList = new ArrayList<>();
    private List<String> inputOutputList = new ArrayList<>();
    private List<String> invalidUrlList = new ArrayList<>();

    public void addSocketException(String url) {
        socketExceptionsList.add(url);
    }

    public void addSocketTimeout(String url) {
        socketTimeoutList.add(url);
    }

    public void addHttpStatusException(String url) {
        httpStatusExceptionsList.add(url);
    }

    public void addUnknownHostException(String url) {
        unknownHostList.add(url);
    }

    public void addInputOutputException(String url) {
        inputOutputList.add(url);
    }

    public void addInvalidUrl(String url) {
        invalidUrlList.add(url);
    }


    public void printExceptions() {
        System.out.println("\nSocketExceptions: " + getSocketExceptionsList().size());
        printList(getSocketExceptionsList());
        System.out.println("\nHttp Status Exceptions: " + getHttpStatusExceptionsList().size());
        printList(getHttpStatusExceptionsList());
        System.out.println("\nSocket Timeout Exceptions: " + getSocketTimeoutList().size());
        printList(getSocketTimeoutList());
        System.out.println("\nUnknown Host Exceptions: " + getUnknownHostList().size());
        printList(getUnknownHostList());
        System.out.println("\nInput Output Exceptions: " + getInputOutputList().size());
        printList(getInputOutputList());
        System.out.println("\nInvalid Urls from the websites json file: " + getInvalidUrlList().size());
        printList(getInvalidUrlList());
        int totalExceptions = getInputOutputList().size() + getSocketExceptionsList().size() + getSocketTimeoutList().size() + getHttpStatusExceptionsList().size() + getUnknownHostList().size() + getInvalidUrlList().size();
        System.out.println("\nTotal Exceptions: " + totalExceptions);
    }

    public static void printList(List<String> list) {
        for (String element : list) {
            System.out.println(element);
        }
    }
}