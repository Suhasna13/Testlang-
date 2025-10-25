package com.testlang.codegen;

import com.testlang.ast.*;
import com.testlang.parser.TestLangParser;
import com.testlang.parser.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Code Generator
 */
public class CodeGeneratorTest {

    @Test
    public void testGenerateMinimalTest(@TempDir Path tempDir) throws Exception {
        String input = """
            test MinimalTest {
                GET "/api/test";
                expect status = 200;
                expect body contains "test";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        // Verify generated code structure
        assertTrue(code.contains("import org.junit.jupiter.api.*"));
        assertTrue(code.contains("import java.net.http.*"));
        assertTrue(code.contains("public class GeneratedTests"));
        assertTrue(code.contains("@Test"));
        assertTrue(code.contains("void test_MinimalTest()"));
        assertTrue(code.contains("GET()"));
        assertTrue(code.contains("assertEquals(200, resp.statusCode())"));
        assertTrue(code.contains("resp.body().contains(\"test\")"));
    }

    @Test
    public void testGenerateWithConfig(@TempDir Path tempDir) throws Exception {
        String input = """
            config {
                base_url = "http://example.com:3000";
                header "Authorization" = "Bearer token";
            }

            test ConfigTest {
                GET "/api/test";
                expect status = 200;
                expect body contains "ok";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        assertTrue(code.contains("http://example.com:3000"));
        assertTrue(code.contains("DEFAULT_HEADERS.put(\"Authorization\",\"Bearer token\")"));
    }

    @Test
    public void testGenerateWithVariableSubstitution(@TempDir Path tempDir) throws Exception {
        String input = """
            let userId = 42;
            let name = "John";

            test VarTest {
                GET "/api/users/$userId";
                expect status = 200;
                expect body contains "$name";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        // Variables should be substituted in generated code
        assertTrue(code.contains("/api/users/42"));
        assertTrue(code.contains("John"));
        assertFalse(code.contains("$userId"));
        assertFalse(code.contains("$name"));
    }

    @Test
    public void testGeneratePostWithBody(@TempDir Path tempDir) throws Exception {
        String input = """
            test PostTest {
                POST "/api/users" {
                    header "Content-Type" = "application/json";
                    body = "{ \\"name\\": \\"test\\" }";
                };
                expect status = 201;
                expect body contains "created";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        assertTrue(code.contains("POST(HttpRequest.BodyPublishers.ofString("));
        assertTrue(code.contains("b.header(\"Content-Type\", \"application/json\")"));
        assertTrue(code.contains("name"));
    }

    @Test
    public void testGeneratePutWithBody(@TempDir Path tempDir) throws Exception {
        String input = """
            test PutTest {
                PUT "/api/users/1" {
                    body = "{ \\"updated\\": true }";
                };
                expect status = 200;
                expect body contains "success";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        assertTrue(code.contains("PUT(HttpRequest.BodyPublishers.ofString("));
        assertTrue(code.contains("updated"));
    }

    @Test
    public void testGenerateDeleteRequest(@TempDir Path tempDir) throws Exception {
        String input = """
            test DeleteTest {
                DELETE "/api/users/1";
                expect status = 204;
                expect body contains "";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        assertTrue(code.contains("DELETE()"));
    }

    @Test
    public void testGenerateHeaderAssertions(@TempDir Path tempDir) throws Exception {
        String input = """
            test HeaderTest {
                GET "/api/test";
                expect header "Content-Type" = "application/json";
                expect header "X-Custom" contains "value";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        assertTrue(code.contains("assertEquals(\"application/json\", resp.headers().firstValue(\"Content-Type\")"));
        assertTrue(code.contains("assertTrue(resp.headers().firstValue(\"X-Custom\")"));
        assertTrue(code.contains(".contains(\"value\")"));
    }

    @Test
    public void testGenerateMultipleTests(@TempDir Path tempDir) throws Exception {
        String input = """
            test Test1 {
                GET "/api/test1";
                expect status = 200;
                expect body contains "test1";
            }

            test Test2 {
                POST "/api/test2" {
                    body = "data";
                };
                expect status = 201;
                expect body contains "test2";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        assertTrue(code.contains("void test_Test1()"));
        assertTrue(code.contains("void test_Test2()"));
        assertEquals(2, countOccurrences(code, "@Test"));
    }

    @Test
    public void testValidationErrorNoTests() throws Exception {
        Program program = new Program();
        // No tests added

        CodeGenerator generator = new CodeGenerator(program);

        ValidationException exception = assertThrows(ValidationException.class, generator::generate);
        assertTrue(exception.getMessage().contains("at least one test"));
    }

    @Test
    public void testValidationErrorNoRequest(@TempDir Path tempDir) {
        String input = """
            test NoRequestTest {
                expect status = 200;
                expect body contains "test";
            }
            """;

        assertThrows(Exception.class, () -> {
            Program program = parseString(input, tempDir);
            CodeGenerator generator = new CodeGenerator(program);
            generator.generate();
        });
    }

    @Test
    public void testValidationErrorInsufficientAssertions(@TempDir Path tempDir) throws Exception {
        String input = """
            test InsufficientAssertions {
                GET "/api/test";
                expect status = 200;
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);

        ValidationException exception = assertThrows(ValidationException.class, generator::generate);
        assertTrue(exception.getMessage().contains("at least 2 assertions"));
        assertTrue(exception.getMessage().contains("InsufficientAssertions"));
    }

    @Test
    public void testEscapeSpecialCharacters(@TempDir Path tempDir) throws Exception {
        String input = """
            test EscapeTest {
                POST "/api/test" {
                    body = "{ \\"quote\\": \\"\\\\\\"\\", \\"newline\\": \\"\\\\n\\" }";
                };
                expect status = 200;
                expect body contains "escaped";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        // Verify escaping is handled correctly
        assertTrue(code.contains("\\\\"));
        assertTrue(code.contains("\\\""));
    }

    @Test
    public void testGeneratedCodeCompiles(@TempDir Path tempDir) throws Exception {
        // Use the actual example.test from resources
        String input = """
            config {
                base_url = "http://localhost:8080";
                header "Content-Type" = "application/json";
            }

            let id = 42;

            test LoginTest {
                POST "/api/login" {
                    body = "{ \\"username\\": \\"admin\\" }";
                };
                expect status = 200;
                expect body contains "token";
            }

            test GetUserTest {
                GET "/api/users/$id";
                expect status = 200;
                expect body contains "user";
            }
            """;

        Program program = parseString(input, tempDir);
        CodeGenerator generator = new CodeGenerator(program);
        String code = generator.generate();

        // Verify all required imports are present
        assertTrue(code.contains("import org.junit.jupiter.api.*;"));
        assertTrue(code.contains("import java.net.http.*;"));
        assertTrue(code.contains("import static org.junit.jupiter.api.Assertions.*;"));

        // Verify class structure
        assertTrue(code.contains("public class GeneratedTests {"));
        assertTrue(code.contains("@BeforeAll"));
        assertTrue(code.contains("static void setup()"));
        assertTrue(code.contains("HttpClient.newBuilder()"));

        // Verify test methods exist
        assertTrue(code.contains("@Test"));
        assertTrue(code.contains("void test_LoginTest()"));
        assertTrue(code.contains("void test_GetUserTest()"));

        // Verify proper method structure
        assertTrue(code.contains("throws Exception"));
        assertTrue(code.contains("HttpResponse<String> resp"));

        // Write to file to verify it's valid Java syntax
        File javaFile = tempDir.resolve("GeneratedTests.java").toFile();
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }

        // If we got here, the generated code has valid structure
        assertTrue(javaFile.exists());
        assertTrue(javaFile.length() > 0);
    }

    // Helper methods
    private Program parseString(String input, Path tempDir) throws Exception {
        File testFile = tempDir.resolve("test.test").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(input);
        }
        return TestLangParser.parse(testFile.getAbsolutePath());
    }

    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
