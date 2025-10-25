package com.testlang.parser;

import com.testlang.ast.Assertion;
import com.testlang.ast.Program;
import com.testlang.ast.Request;
import com.testlang.ast.Header;
import com.testlang.ast.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
//import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Parser (syntax analysis and AST building)
 */
public class ParserTest {

    @Test
    public void testParseMinimalTest(@TempDir Path tempDir) throws Exception {
        String input = """
            test MinimalTest {
                GET "/api/test";
                expect status = 200;
                expect body contains "test";
            }
            """;

        Program program = parseString(input, tempDir);

        assertNotNull(program);
        assertEquals(1, program.getTests().size());

        com.testlang.ast.Test test = program.getTests().get(0);
        assertEquals("MinimalTest", test.getName());
        assertEquals(3, test.getStatements().size());
    }

    @Test
    public void testParseConfigBlock(@TempDir Path tempDir) throws Exception {
        String input = """
            config {
                base_url = "http://localhost:8080";
                header "Content-Type" = "application/json";
                header "X-App" = "TestApp";
            }

            test SomeTest {
                GET "/api/test";
                expect status = 200;
                expect body contains "ok";
            }
            """;

        Program program = parseString(input, tempDir);

        assertNotNull(program.getConfig());
        assertEquals("http://localhost:8080", program.getConfig().getBaseUrl());
        assertEquals(2, program.getConfig().getHeaders().size());

        Header h1 = program.getConfig().getHeaders().get(0);
        assertEquals("Content-Type", h1.getKey());
        assertEquals("application/json", h1.getValue());

        Header h2 = program.getConfig().getHeaders().get(1);
        assertEquals("X-App", h2.getKey());
        assertEquals("TestApp", h2.getValue());
    }

    @Test
    public void testParseVariableDeclarations(@TempDir Path tempDir) throws Exception {
        String input = """
            let userId = 42;
            let username = "testuser";
            let active = 1;

            test SomeTest {
                GET "/api/test";
                expect status = 200;
                expect body contains "test";
            }
            """;

        Program program = parseString(input, tempDir);

        assertEquals(3, program.getVariables().size());

        Variable v1 = program.getVariables().get(0);
        assertEquals("userId", v1.getName());
        assertEquals(42, v1.getValue());

        Variable v2 = program.getVariables().get(1);
        assertEquals("username", v2.getName());
        assertEquals("testuser", v2.getValue());

        Variable v3 = program.getVariables().get(2);
        assertEquals("active", v3.getName());
        assertEquals(1, v3.getValue());
    }

    @Test
    public void testParseGetRequest(@TempDir Path tempDir) throws Exception {
        String input = """
            test GetTest {
                GET "/api/users/42";
                expect status = 200;
                expect body contains "user";
            }
            """;

        Program program = parseString(input, tempDir);
        com.testlang.ast.Test test = program.getTests().get(0);
        Request req = (Request) test.getStatements().get(0);

        assertEquals("GET", req.getMethod());
        assertEquals("/api/users/42", req.getUrl());
        assertTrue(req.getHeaders().isEmpty());
        assertNull(req.getBody());
    }

    @Test
    public void testParsePostRequest(@TempDir Path tempDir) throws Exception {
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
        com.testlang.ast.Test test = program.getTests().get(0);
        Request req = (Request) test.getStatements().get(0);

        assertEquals("POST", req.getMethod());
        assertEquals("/api/users", req.getUrl());
        assertEquals(1, req.getHeaders().size());
        assertEquals("Content-Type", req.getHeaders().get(0).getKey());
        assertEquals("application/json", req.getHeaders().get(0).getValue());
        assertNotNull(req.getBody());
        assertTrue(req.getBody().contains("name"));
    }

    @Test
    public void testParsePutRequest(@TempDir Path tempDir) throws Exception {
        String input = """
            test PutTest {
                PUT "/api/users/42" {
                    body = "{ \\"updated\\": true }";
                };
                expect status = 200;
                expect body contains "success";
            }
            """;

        Program program = parseString(input, tempDir);
        com.testlang.ast.Test test = program.getTests().get(0);
        Request req = (Request) test.getStatements().get(0);

        assertEquals("PUT", req.getMethod());
        assertEquals("/api/users/42", req.getUrl());
        assertNotNull(req.getBody());
    }

    @Test
    public void testParseDeleteRequest(@TempDir Path tempDir) throws Exception {
        String input = """
            test DeleteTest {
                DELETE "/api/users/42";
                expect status = 204;
                expect body contains "";
            }
            """;

        Program program = parseString(input, tempDir);
        com.testlang.ast.Test test = program.getTests().get(0);
        Request req = (Request) test.getStatements().get(0);

        assertEquals("DELETE", req.getMethod());
        assertEquals("/api/users/42", req.getUrl());
    }

    @Test
    public void testParseStatusAssertion(@TempDir Path tempDir) throws Exception {
        String input = """
            test StatusTest {
                GET "/api/test";
                expect status = 404;
                expect body contains "not found";
            }
            """;

        Program program = parseString(input, tempDir);
        com.testlang.ast.Test test = program.getTests().get(0);
        Assertion assertion = (Assertion) test.getStatements().get(1);

        assertEquals(Assertion.Type.STATUS_EQUALS, assertion.getType());
        assertEquals(404, assertion.getStatusCode());
    }

    @Test
    public void testParseHeaderAssertions(@TempDir Path tempDir) throws Exception {
        String input = """
            test HeaderTest {
                GET "/api/test";
                expect header "Content-Type" = "application/json";
                expect header "X-Custom" contains "value";
            }
            """;

        Program program = parseString(input, tempDir);
        com.testlang.ast.Test test = program.getTests().get(0);

        Assertion a1 = (Assertion) test.getStatements().get(1);
        assertEquals(Assertion.Type.HEADER_EQUALS, a1.getType());
        assertEquals("Content-Type", a1.getKey());
        assertEquals("application/json", a1.getValue());

        Assertion a2 = (Assertion) test.getStatements().get(2);
        assertEquals(Assertion.Type.HEADER_CONTAINS, a2.getType());
        assertEquals("X-Custom", a2.getKey());
        assertEquals("value", a2.getValue());
    }

    @Test
    public void testParseBodyAssertion(@TempDir Path tempDir) throws Exception {
        String input = """
            test BodyTest {
                GET "/api/test";
                expect status = 200;
                expect body contains "\\"id\\": 42";
            }
            """;

        Program program = parseString(input, tempDir);
        com.testlang.ast.Test test = program.getTests().get(0);
        Assertion assertion = (Assertion) test.getStatements().get(2);

        assertEquals(Assertion.Type.BODY_CONTAINS, assertion.getType());
        assertTrue(assertion.getValue().contains("id"));
    }

    @Test
    public void testParseMultipleTests(@TempDir Path tempDir) throws Exception {
        String input = """
            test Test1 {
                GET "/api/test1";
                expect status = 200;
                expect body contains "test1";
            }

            test Test2 {
                GET "/api/test2";
                expect status = 200;
                expect body contains "test2";
            }

            test Test3 {
                POST "/api/test3" {
                    body = "data";
                };
                expect status = 201;
                expect body contains "created";
            }
            """;

        Program program = parseString(input, tempDir);

        assertEquals(3, program.getTests().size());
        assertEquals("Test1", program.getTests().get(0).getName());
        assertEquals("Test2", program.getTests().get(1).getName());
        assertEquals("Test3", program.getTests().get(2).getName());
    }

    @Test
    public void testParseFullFeatureProgram(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("valid-full.test").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("""
                config {
                    base_url = "http://localhost:8080";
                    header "Content-Type" = "application/json";
                }

                let userId = 42;
                let username = "testuser";

                test GetTest {
                    GET "/api/users/$userId";
                    expect status = 200;
                    expect body contains "user";
                }

                test PostTest {
                    POST "/api/users" {
                        body = "{ \\"username\\": \\"$username\\" }";
                    };
                    expect status = 201;
                    expect body contains "created";
                }
                """);
        }

        Program program = TestLangParser.parse(testFile.getAbsolutePath());

        assertNotNull(program);
        assertNotNull(program.getConfig());
        assertEquals(2, program.getVariables().size());
        assertEquals(2, program.getTests().size());
    }

    @Test
    public void testParseSyntaxError(@TempDir Path tempDir) {
        String input = """
            test BadTest {
                GET "/api/test"
                expect status = 200;
            }
            """;

        assertThrows(Exception.class, () -> parseString(input, tempDir));
    }

    // Helper method to parse a string input
    private Program parseString(String input, Path tempDir) throws Exception {
        File testFile = tempDir.resolve("test.test").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(input);
        }
        return TestLangParser.parse(testFile.getAbsolutePath());
    }
}
