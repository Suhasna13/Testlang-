package com.testlang.parser;

/**
 * Exception thrown during parsing (syntax analysis)
 */
public class ParserException extends Exception {
    private final int line;
    private final String context;

    public ParserException(String message, int line) {
        this(message, line, null);
    }

    public ParserException(String message, int line, String context) {
        super(formatMessage(message, line, context));
        this.line = line;
        this.context = context;
    }

    private static String formatMessage(String message, int line, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Parse error at line ").append(line).append(": ").append(message);
        if (context != null && !context.isEmpty()) {
            sb.append("\n  Context: ").append(context);
        }
        return sb.toString();
    }

    public int getLine() {
        return line;
    }

    public String getContext() {
        return context;
    }
}
