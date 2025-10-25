package com.testlang.ast;

/**
 * Represents a variable declaration (let name = value;)
 */
public class Variable {
    private String name;
    private Object value; // Can be String or Integer

    public Variable(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        return value.toString();
    }
}
