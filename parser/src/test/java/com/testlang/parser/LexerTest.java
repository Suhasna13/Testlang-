package com.testlang.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java_cup.runtime.Symbol;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Lexer (scanner)
 */
public class LexerTest {

    private Lexer createLexer(String input) {
        return new Lexer(new StringReader(input));
    }

    @Test
    public void testKeywords() throws Exception {
        String input = "config base_url header let test GET POST PUT DELETE expect status body contains";
        Lexer lexer = createLexer(input);

        assertEquals(sym.CONFIG, lexer.next_token().sym);
        assertEquals(sym.BASE_URL, lexer.next_token().sym);
        assertEquals(sym.HEADER, lexer.next_token().sym);
        assertEquals(sym.LET, lexer.next_token().sym);
        assertEquals(sym.TEST, lexer.next_token().sym);
        assertEquals(sym.GET, lexer.next_token().sym);
        assertEquals(sym.POST, lexer.next_token().sym);
        assertEquals(sym.PUT, lexer.next_token().sym);
        assertEquals(sym.DELETE, lexer.next_token().sym);
        assertEquals(sym.EXPECT, lexer.next_token().sym);
        assertEquals(sym.STATUS, lexer.next_token().sym);
        assertEquals(sym.BODY, lexer.next_token().sym);
        assertEquals(sym.CONTAINS, lexer.next_token().sym);
    }

    @Test
    public void testOperators() throws Exception {
        String input = "{ } ; =";
        Lexer lexer = createLexer(input);

        assertEquals(sym.LBRACE, lexer.next_token().sym);
        assertEquals(sym.RBRACE, lexer.next_token().sym);
        assertEquals(sym.SEMICOLON, lexer.next_token().sym);
        assertEquals(sym.EQUALS, lexer.next_token().sym);
    }

    @ParameterizedTest
    @ValueSource(strings = {"myVar", "_private", "test123", "camelCase", "CONSTANT_NAME"})
    public void testValidIdentifiers(String identifier) throws Exception {
        Lexer lexer = createLexer(identifier);
        Symbol token = lexer.next_token();

        assertEquals(sym.IDENTIFIER, token.sym);
        assertEquals(identifier, token.value);
    }

    @Test
    public void testNumbers() throws Exception {
        String input = "0 42 200 404 9999";
        Lexer lexer = createLexer(input);

        Symbol t1 = lexer.next_token();
        assertEquals(sym.NUMBER, t1.sym);
        assertEquals(0, t1.value);

        Symbol t2 = lexer.next_token();
        assertEquals(sym.NUMBER, t2.sym);
        assertEquals(42, t2.value);

        Symbol t3 = lexer.next_token();
        assertEquals(sym.NUMBER, t3.sym);
        assertEquals(200, t3.value);

        Symbol t4 = lexer.next_token();
        assertEquals(sym.NUMBER, t4.sym);
        assertEquals(404, t4.value);

        Symbol t5 = lexer.next_token();
        assertEquals(sym.NUMBER, t5.sym);
        assertEquals(9999, t5.value);
    }

    @Test
    public void testSimpleStrings() throws Exception {
        String input = "\"hello\" \"world\" \"test123\"";
        Lexer lexer = createLexer(input);

        Symbol t1 = lexer.next_token();
        assertEquals(sym.STRING, t1.sym);
        assertEquals("hello", t1.value);

        Symbol t2 = lexer.next_token();
        assertEquals(sym.STRING, t2.sym);
        assertEquals("world", t2.value);

        Symbol t3 = lexer.next_token();
        assertEquals(sym.STRING, t3.sym);
        assertEquals("test123", t3.value);
    }

    @Test
    public void testStringWithEscapes() throws Exception {
        String input = "\"hello\\\"world\\\"\" \"path\\\\to\\\\file\"";
        Lexer lexer = createLexer(input);

        Symbol t1 = lexer.next_token();
        assertEquals(sym.STRING, t1.sym);
        assertEquals("hello\"world\"", t1.value);

        Symbol t2 = lexer.next_token();
        assertEquals(sym.STRING, t2.sym);
        assertEquals("path\\to\\file", t2.value);
    }

    @Test
    public void testComments() throws Exception {
        String input = "let x = \"test\"; // this is a comment\nlet y = \"test2\";";
        Lexer lexer = createLexer(input);

        assertEquals(sym.LET, lexer.next_token().sym);
        assertEquals(sym.IDENTIFIER, lexer.next_token().sym);
        assertEquals(sym.EQUALS, lexer.next_token().sym);
        assertEquals(sym.STRING, lexer.next_token().sym);
        assertEquals(sym.SEMICOLON, lexer.next_token().sym);
        // Comment should be ignored
        assertEquals(sym.LET, lexer.next_token().sym);
        assertEquals(sym.IDENTIFIER, lexer.next_token().sym);
        assertEquals(sym.EQUALS, lexer.next_token().sym);
        assertEquals(sym.STRING, lexer.next_token().sym);
        assertEquals(sym.SEMICOLON, lexer.next_token().sym);
    }

    @Test
    public void testWhitespace() throws Exception {
        String input = "let   \t  x  \n  =\r\n\"test\"  ;";
        Lexer lexer = createLexer(input);

        assertEquals(sym.LET, lexer.next_token().sym);
        assertEquals(sym.IDENTIFIER, lexer.next_token().sym);
        assertEquals(sym.EQUALS, lexer.next_token().sym);
        assertEquals(sym.STRING, lexer.next_token().sym);
        assertEquals(sym.SEMICOLON, lexer.next_token().sym);
    }

    @Test
    public void testLineAndColumnTracking() throws Exception {
        String input = "let x = 42;\nlet y = 100;";
        Lexer lexer = createLexer(input);

        Symbol t1 = lexer.next_token();
        assertEquals(1, t1.left); // line
        assertEquals(1, t1.right); // column

        // Skip to second line
        lexer.next_token(); // x
        lexer.next_token(); // =
        lexer.next_token(); // 42
        lexer.next_token(); // ;

        Symbol t2 = lexer.next_token(); // second let
        assertEquals(2, t2.left); // line 2
    }

    @Test
    public void testUnterminatedString() {
        String input = "let x = \"unterminated;";
        Lexer lexer = createLexer(input);

        assertThrows(LexerException.class, () -> {
            lexer.next_token(); // let
            lexer.next_token(); // x
            lexer.next_token(); // =
            lexer.next_token(); // should throw on unterminated string
        });
    }

    @Test
    public void testInvalidIdentifier() {
        String input = "let 2bad = \"value\";";
        Lexer lexer = createLexer(input);

        assertThrows(LexerException.class, () -> {
            lexer.next_token(); // let
            lexer.next_token(); // should throw on 2bad
        });
    }

    @Test
    public void testIllegalCharacter() {
        String input = "let x @ \"test\";";
        Lexer lexer = createLexer(input);

        assertThrows(LexerException.class, () -> {
            lexer.next_token(); // let
            lexer.next_token(); // x
            lexer.next_token(); // should throw on @
        });
    }

    @Test
    public void testCompleteProgram() throws Exception {
        String input = """
            config {
                base_url = "http://localhost:8080";
            }

            let id = 42;

            test MyTest {
                GET "/api/users/$id";
                expect status = 200;
                expect body contains "user";
            }
            """;

        Lexer lexer = createLexer(input);

        // Just verify it tokenizes without errors
        Symbol token;
        int tokenCount = 0;
        while ((token = lexer.next_token()).sym != sym.EOF) {
            tokenCount++;
            assertNotNull(token);
        }

        assertTrue(tokenCount > 0, "Should have tokenized some tokens");
    }
}
