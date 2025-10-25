package com.testlang.ast;

/**
 * Represents an assertion (expect statement)
 */
public class Assertion implements Statement {
    public enum Type {
        STATUS_EQUALS,        // expect status = 200
        HEADER_EQUALS,        // expect header "K" = "V"
        HEADER_CONTAINS,      // expect header "K" contains "V"
        BODY_CONTAINS         // expect body contains "V"
    }

    private Type type;
    private String key;       // For header assertions
    private String value;     // Expected value or substring
    private Integer statusCode; // For status assertions

    public Assertion(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}
