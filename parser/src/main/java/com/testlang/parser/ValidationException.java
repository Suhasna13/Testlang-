package com.testlang.parser;

/**
 * Exception thrown during semantic validation
 */
public class ValidationException extends Exception {
    private final String testName;

    public ValidationException(String message) {
        this(message, null);
    }

    public ValidationException(String message, String testName) {
        super(formatMessage(message, testName));
        this.testName = testName;
    }

    private static String formatMessage(String message, String testName) {
        if (testName != null) {
            return "Validation error in test '" + testName + "': " + message;
        }
        return "Validation error: " + message;
    }

    public String getTestName() {
        return testName;
    }
}
