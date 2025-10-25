package com.testlang.codegen;

import com.testlang.ast.*;
import com.testlang.parser.ValidationException;
import java.util.*;

/**
 * Generates JUnit 5 test code from the AST
 */
public class CodeGenerator {
    private Program program;
    private StringBuilder code;
    private Map<String, String> variables;

    public CodeGenerator(Program program) {
        this.program = program;
        this.code = new StringBuilder();
        this.variables = new HashMap<>();
    }

    public String generate() throws ValidationException {
        // Validate the program before generating code
        validate();

        // Store variables
        for (Variable var : program.getVariables()) {
            variables.put(var.getName(), var.getValueAsString());
        }

        // Generate class header
        generateImports();
        generateClassHeader();
        generateSetupMethod();

        // Generate test methods
        for (Test test : program.getTests()) {
            generateTestMethod(test);
        }

        // Close class
        code.append("}\n");

        return code.toString();
    }

    /**
     * Validates the AST before code generation
     */
    private void validate() throws ValidationException {
        // At least one test is required
        if (program.getTests().isEmpty()) {
            throw new ValidationException("Program must contain at least one test block");
        }

        // Validate each test
        for (Test test : program.getTests()) {
            validateTest(test);
        }

        // Warn about unused variables (non-fatal)
        checkUnusedVariables();
    }

    private void validateTest(Test test) throws ValidationException {
        String testName = test.getName();
        int requestCount = 0;
        int assertionCount = 0;

        for (Statement stmt : test.getStatements()) {
            if (stmt instanceof Request) {
                requestCount++;
            } else if (stmt instanceof Assertion) {
                assertionCount++;
            }
        }

        // Each test must have at least 1 request
        if (requestCount == 0) {
            throw new ValidationException(
                "Test must contain at least one HTTP request (GET/POST/PUT/DELETE)",
                testName
            );
        }

        // Each test must have at least 2 assertions
        if (assertionCount < 2) {
            throw new ValidationException(
                "Test must contain at least 2 assertions (found " + assertionCount + ")",
                testName
            );
        }
    }

    private void checkUnusedVariables() {
        Set<String> usedVars = new HashSet<>();

        // Check which variables are actually used
        for (Test test : program.getTests()) {
            for (Statement stmt : test.getStatements()) {
                if (stmt instanceof Request) {
                    Request req = (Request) stmt;
                    checkVariableUsage(req.getUrl(), usedVars);
                    if (req.getBody() != null) {
                        checkVariableUsage(req.getBody(), usedVars);
                    }
                }
            }
        }

        // Warn about unused variables
        for (Variable var : program.getVariables()) {
            if (!usedVars.contains(var.getName())) {
                System.err.println("Warning: Variable '" + var.getName() + "' is declared but never used");
            }
        }
    }

    private void checkVariableUsage(String text, Set<String> usedVars) {
        for (String varName : variables.keySet()) {
            if (text.contains("$" + varName)) {
                usedVars.add(varName);
            }
        }
    }

    private void generateImports() {
        code.append("import org.junit.jupiter.api.*;\n");
        code.append("import static org.junit.jupiter.api.Assertions.*;\n");
        code.append("import java.net.http.*;\n");
        code.append("import java.net.*;\n");
        code.append("import java.time.Duration;\n");
        code.append("import java.nio.charset.StandardCharsets;\n");
        code.append("import java.util.*;\n\n");
    }

    private void generateClassHeader() {
        code.append("public class GeneratedTests {\n\n");

        // Static fields
        String baseUrl = "http://localhost:8080";
        if (program.getConfig() != null && program.getConfig().getBaseUrl() != null) {
            baseUrl = program.getConfig().getBaseUrl();
        }
        code.append("    static String BASE = \"").append(baseUrl).append("\";\n");
        code.append("    static Map<String,String> DEFAULT_HEADERS = new HashMap<>();\n");
        code.append("    static HttpClient client;\n\n");
    }

    private void generateSetupMethod() {
        code.append("    @BeforeAll\n");
        code.append("    static void setup() {\n");
        code.append("        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();\n");

        // Add default headers from config
        if (program.getConfig() != null) {
            for (Header header : program.getConfig().getHeaders()) {
                code.append("        DEFAULT_HEADERS.put(\"")
                    .append(escapeJava(header.getKey()))
                    .append("\",\"")
                    .append(escapeJava(header.getValue()))
                    .append("\");\n");
            }
        }

        code.append("    }\n\n");
    }

    private void generateTestMethod(Test test) {
        code.append("    @Test\n");
        code.append("    void test_").append(test.getName()).append("() throws Exception {\n");

        for (Statement stmt : test.getStatements()) {
            if (stmt instanceof Request) {
                generateRequest((Request) stmt);
            } else if (stmt instanceof Assertion) {
                generateAssertion((Assertion) stmt);
            }
        }

        code.append("    }\n\n");
    }

    private void generateRequest(Request request) {
        String url = substituteVariables(request.getUrl());

        // Build full URL
        String fullUrl;
        if (url.startsWith("/")) {
            fullUrl = "BASE + \"" + url + "\"";
        } else {
            fullUrl = "\"" + url + "\"";
        }

        code.append("        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(")
            .append(fullUrl)
            .append("))\n");
        code.append("            .timeout(Duration.ofSeconds(10))\n");

        // Add HTTP method
        switch (request.getMethod()) {
            case "GET":
                code.append("            .GET();\n");
                break;
            case "DELETE":
                code.append("            .DELETE();\n");
                break;
            case "POST":
                if (request.getBody() != null) {
                    String body = substituteVariables(request.getBody());
                    code.append("            .POST(HttpRequest.BodyPublishers.ofString(\"")
                        .append(escapeJava(body))
                        .append("\"));\n");
                } else {
                    code.append("            .POST(HttpRequest.BodyPublishers.noBody());\n");
                }
                break;
            case "PUT":
                if (request.getBody() != null) {
                    String body = substituteVariables(request.getBody());
                    code.append("            .PUT(HttpRequest.BodyPublishers.ofString(\"")
                        .append(escapeJava(body))
                        .append("\"));\n");
                } else {
                    code.append("            .PUT(HttpRequest.BodyPublishers.noBody());\n");
                }
                break;
        }

        // Add request-specific headers
        for (Header header : request.getHeaders()) {
            code.append("        b.header(\"")
                .append(escapeJava(header.getKey()))
                .append("\", \"")
                .append(escapeJava(header.getValue()))
                .append("\");\n");
        }

        // Add default headers
        code.append("        for (var e: DEFAULT_HEADERS.entrySet()) b.header(e.getKey(), e.getValue());\n");

        // Send request
        code.append("        HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));\n\n");
    }

    private void generateAssertion(Assertion assertion) {
        String key = assertion.getKey() != null ? substituteVariables(assertion.getKey()) : null;
        String value = assertion.getValue() != null ? substituteVariables(assertion.getValue()) : null;

        switch (assertion.getType()) {
            case STATUS_EQUALS:
                code.append("        assertEquals(")
                        .append(assertion.getStatusCode())
                        .append(", resp.statusCode());\n");
                break;

            case HEADER_EQUALS:
                code.append("        assertEquals(\"")
                        .append(escapeJava(value))
                        .append("\", resp.headers().firstValue(\"")
                        .append(escapeJava(key))
                        .append("\").orElse(\"\"));\n");
                break;

            case HEADER_CONTAINS:
                code.append("        assertTrue(resp.headers().firstValue(\"")
                        .append(escapeJava(key))
                        .append("\").orElse(\"\").contains(\"")
                        .append(escapeJava(value))
                        .append("\"));\n");
                break;

            case BODY_CONTAINS:
                code.append("        assertTrue(resp.body().contains(\"")
                        .append(escapeJava(value))
                        .append("\"));\n");
                break;
        }
    }


    private String substituteVariables(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("$" + entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String escapeJava(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
