package com.testlang.parser;

/**
 * Exception thrown during lexical analysis (scanning)
 */
public class LexerException extends Exception {
    private final int line;
    private final int column;
    private final String problematicText;

    public LexerException(String message, int line, int column, String problematicText) {
        super(formatMessage(message, line, column, problematicText));
        this.line = line;
        this.column = column;
        this.problematicText = problematicText;
    }

    private static String formatMessage(String message, int line, int column, String text) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lexer error at line ").append(line).append(", column ").append(column);
        sb.append(": ").append(message);
        if (text != null && !text.isEmpty()) {
            sb.append("\n  Near: '").append(escapeForDisplay(text)).append("'");
        }
        return sb.toString();
    }

    private static String escapeForDisplay(String text) {
        if (text.length() > 20) {
            text = text.substring(0, 20) + "...";
        }
        return text.replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getProblematicText() {
        return problematicText;
    }
}
