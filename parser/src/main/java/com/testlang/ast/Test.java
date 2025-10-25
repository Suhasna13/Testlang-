package com.testlang.ast;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a test block with requests and assertions
 */
public class Test {
    private String name;
    private List<Statement> statements;

    public Test(String name) {
        this.name = name;
        this.statements = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void addStatement(Statement statement) {
        this.statements.add(statement);
    }
}
