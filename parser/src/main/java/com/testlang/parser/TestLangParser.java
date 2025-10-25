package com.testlang.parser;

import com.testlang.ast.Program;
import com.testlang.codegen.CodeGenerator;
import java_cup.runtime.Symbol;
import java.io.*;

/**
 * Main entry point for the TestLang++ parser
 */
public class TestLangParser {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestLangParser <input.test> [output.java]");
            System.err.println("  If output file is not specified, generates GeneratedTests.java");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args.length > 1 ? args[1] : "GeneratedTests.java";

        try {
            // Parse the input file
            Program program = parse(inputFile);

            // Generate code
            CodeGenerator generator = new CodeGenerator(program);
            String generatedCode = generator.generate();

            // Write to output file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.print(generatedCode);
            }

            System.out.println("Successfully generated " + outputFile);

        } catch (LexerException e) {
            System.err.println("\n=== LEXER ERROR ===");
            System.err.println(e.getMessage());
            System.err.println("\nPlease fix the syntax error and try again.");
            System.exit(1);

        } catch (ParserException e) {
            System.err.println("\n=== PARSER ERROR ===");
            System.err.println(e.getMessage());
            System.err.println("\nPlease check your .test file syntax.");
            System.exit(1);

        } catch (ValidationException e) {
            System.err.println("\n=== VALIDATION ERROR ===");
            System.err.println(e.getMessage());
            System.err.println("\nPlease ensure your tests meet the requirements:");
            System.err.println("  - At least 1 HTTP request per test");
            System.err.println("  - At least 2 assertions per test");
            System.exit(1);

        } catch (FileNotFoundException e) {
            System.err.println("\n=== FILE ERROR ===");
            System.err.println("Input file not found: " + inputFile);
            System.exit(1);

        } catch (IOException e) {
            System.err.println("\n=== IO ERROR ===");
            System.err.println("Error reading/writing files: " + e.getMessage());
            System.exit(1);

        } catch (Exception e) {
            System.err.println("\n=== UNEXPECTED ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Program parse(String filename) throws Exception {
        try (FileReader fileReader = new FileReader(filename)) {
            Lexer lexer = new Lexer(fileReader);
            parser parser = new parser(lexer);

            Symbol result = parser.parse();
            return (Program) result.value;
        }
    }

}
