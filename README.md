# TestLang++ DSL - HTTP API Testing Language

## 🎯 What Is This Project?

This is a **custom programming language** (Domain-Specific Language) that makes writing HTTP API tests simple. Instead of complex Java code, you write easy-to-read test files that automatically become runnable JUnit tests!

**Example - What you write:**
```
test Login {
    POST "/api/login" {
        body = "{ \"username\": \"admin\" }";
    };
    expect status = 200;
    expect body contains "token";
}
```

**What it becomes:** Fully functional Java JUnit test code that tests your API!

---

## 📂 Project Files Explained

### Main Folders

#### 📁 `backend/` - The Test API Server
**Purpose:** A working web server to test against  
**Contains:** Spring Boot REST API with 3 endpoints  
**Key File:** `TestLangDemoApplication.java` - The server program  
**Build Output:** `target/testlang-demo-0.0.1-SNAPSHOT.jar` (17 MB)

**What it does:** Provides a real API for testing (login, get user, update user)

---

#### 📁 `parser/` - The Language Magic
**Purpose:** Converts your simple .test files into Java code  
**This is the core of the assignment!**

**Key Files:**

| File | What It Does | Assignment Part |
|------|--------------|-----------------|
| `lexer.flex` | Reads your test file word-by-word | Scanner (JFlex) |
| `parser.cup` | Understands the grammar/syntax | Parser (CUP) |
| `CodeGenerator.java` | Creates Java JUnit code | Code Generation |
| `LexerException.java` | Handles reading errors | Error Handling |
| `ParserException.java` | Handles grammar errors | Error Handling |
| `ValidationException.java` | Validates test logic | Semantic Validation |

**Test Files (44 comprehensive tests):**
- `LexerTest.java` - 16 tests for word scanning
- `ParserTest.java` - 14 tests for grammar parsing  
- `CodeGeneratorTest.java` - 14 tests for code generation

**Build Output:** `target/testlang-parser-1.0-SNAPSHOT-jar-with-dependencies.jar` (4.2 MB)

---

### Important Files in Root

#### `example.test`
**Purpose:** Shows how to write tests in TestLang++  
**Contains:** 3 complete test cases covering all backend endpoints  
**Use it:** As a template for writing your own tests

#### `invalid.test`
**Purpose:** Examples of incorrect syntax  
**Shows:** What errors look like and how they're reported

#### `GeneratedTests.java` 
**Purpose:** The Java code created from `example.test`  
**Shows:** What your simple tests become in Java

---

## ✅ Assignment Requirements & How They're Met

| Requirement | How Achieved | Files/Evidence |
|------------|--------------|----------------|
| **1. Design DSL** | Created TestLang++ with config, variables, tests, HTTP methods, assertions | `example.test` |
| **2. JFlex Scanner** | Tokenizes input (keywords, strings, numbers, operators) | `lexer.flex` |
| **3. CUP Parser** | Parses grammar, builds AST (Abstract Syntax Tree) | `parser.cup` |
| **4. Generate JUnit 5** | Converts AST to Java code using HttpClient | `CodeGenerator.java` |
| **5. Java 11+ HttpClient** | No third-party HTTP libraries used | `GeneratedTests.java` |
| **6. Error Messages** | Line numbers, context, helpful messages | `*Exception.java` files |
| **7. Local Backend** | Spring Boot API with 3 endpoints | `TestLangDemoApplication.java` |
| **8. Runnable Tests** | Generated tests execute and pass | All 3 tests passing |

---

## 🚀 How To Run Everything (Simple Steps)

### Prerequisites
✅ Java 11 or newer  
✅ Maven 3.6+  
✅ Terminal/Command Prompt

---

### STEP 1: Build The Project

**What this does:** Compiles everything and creates runnable programs  
**Why:** You need to build before running!

***Method 1 - If Maven is installed globally (i.e., the mvn command works in your terminal)*** 
```bash
# Build the backend API server
cd backend
mvn clean package
cd ..

# Build the DSL parser
cd parser
mvn clean package
cd ..
```
***Method 2 - Using IDE Maven Sidebar***   

1. Open your IDE (VS Code, IntelliJ, or Eclipse) and open the project folder.
2. Locate the Maven Sidebar / Tool Window.

3. Build the backend API server:
   - In the Maven sidebar, expand the `backend` project.
   - Find the Lifecycle section.
   - Double-click `clean`, then `package`.

4. Build the DSL parser:
   - Expand the `parser` project in the Maven sidebar.
   - Double-click `clean`, then `package`.  

**Success looks like:** Both commands end with `BUILD SUCCESS`

**What you get:**
- `backend/target/testlang-demo-0.0.1-SNAPSHOT.jar` ← Your API server
- `parser/target/testlang-parser-1.0-SNAPSHOT-jar-with-dependencies.jar` ← Your test generator

---

### STEP 2: Start The API Server

**What this does:** Runs a web server at http://localhost:8080  
**Why:** You need a running API to test!

**Open Terminal Window #1:**
```bash
cd backend
```
```bash
java -jar target/testlang-demo-0.0.1-SNAPSHOT.jar
```

**Success looks like:**
```
Tomcat started on port(s): 8080
Started TestLangDemoApplication
```

**⚠️ KEEP THIS WINDOW OPEN** - the server must stay running!

---

### STEP 3: Generate Tests

**What this does:** Converts `example.test` into Java code  
**Why:** This proves your DSL works!

**Open Terminal Window #2:**

Navigate to `project root`  
*If you are already in the project root, you can skip this step.*
```bash
# Navigate to project root
cd ".\IT23632332 - U. T. S. Ranathunga"
```

```bash
# Run the parser
java -jar parser/target/testlang-parser-1.0-SNAPSHOT-jar-with-dependencies.jar example.test GeneratedTests.java
```

**Success looks like:**
```
Successfully generated GeneratedTests.java
```

**What happened:**
- Input: `example.test` (your simple DSL)
- Output: `GeneratedTests.java` (complex Java code)
- Magic: The parser did all the work!

---

### STEP 4: Compile The Generated Tests

**What this does:** Compiles the Java code  
**Why:** Java needs compilation before execution

```bash
javac -cp "parser/target/testlang-parser-1.0-SNAPSHOT-jar-with-dependencies.jar;~/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.9.3/junit-jupiter-api-5.9.3.jar;~/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.9.3/junit-jupiter-engine-5.9.3.jar;." GeneratedTests.java
```

**Success:** No output means it worked!  
**You now have:** `GeneratedTests.class` (compiled bytecode)

---

### STEP 5: Run The Tests

**What this does:** Executes your tests against the API  
**Why:** This proves everything works end-to-end!

First, create a simple test runner:  
Which is already created here. (`RunTests.java`)

Now compile and run:

```bash
# Compile the runner
javac -cp "parser/target/testlang-parser-1.0-SNAPSHOT-jar-with-dependencies.jar;C:\Users\VICTUS\.m2\repository\org\junit\jupiter\junit-jupiter-api\5.9.3\junit-jupiter-api-5.9.3.jar;C:\Users\VICTUS\.m2\repository\org\junit\jupiter\junit-jupiter-engine\5.9.3\junit-jupiter-engine-5.9.3.jar;." RunTests.java
```
```bash
# Run all tests!
java -cp "parser/target/testlang-parser-1.0-SNAPSHOT-jar-with-dependencies.jar;C:\Users\VICTUS\.m2\repository\org\junit\jupiter\junit-jupiter-api\5.9.3\junit-jupiter-api-5.9.3.jar;C:\Users\VICTUS\.m2\repository\org\junit\jupiter\junit-jupiter-engine\5.9.3\junit-jupiter-engine-5.9.3.jar;." RunTests
```

**Success looks like:**
```
===== RUNNING TESTS =====

✓ Login Test PASSED
✓ GetUser Test PASSED
✓ UpdateUser Test PASSED

===== RESULTS =====
Total:  3
Passed: 3
Failed: 0
===================
```

**🎉 All tests passed!** Your DSL works perfectly!

### If you want to clean the project and start again

***Remove the generated files and compiled class files**
```bash
# Remove the compiled class files
Remove-Item -Force .\GeneratedTests.class, .\RunTests.class
```
```bash
# Remove the GeneratedTests.java class file
Remove-Item -Force .\GeneratedTests.java
```
**Then start from STEP 1**

---

## 🔍 What Each Test Does

### Test 1: Login (`test_Login`)
**Tests:** POST request with JSON body  
**Sends:** Username and password to `/api/login`  
**Checks:** 
- Status code is 200
- Response contains "token"
- Headers contain "json"

### Test 2: Get User (`test_GetUser`)
**Tests:** GET request with URL parameter  
**Sends:** GET to `/api/users/42`  
**Checks:**
- Status code is 200
- Response body contains user ID 42

### Test 3: Update User (`test_UpdateUser`)
**Tests:** PUT request with headers and body  
**Sends:** Update role to ADMIN for user 42  
**Checks:**
- Status code is 200
- Custom header "X-App" is correct
- Response shows updated=true
- Response shows new role

---

## 🛠️ Technology Explained Simply

### JFlex (`lexer.flex`)
**Think:** Reading a book word by word  
**Does:** Breaks your .test file into tokens (words, numbers, symbols)  
**Example:** `test Login {` → [test] [Login] [{]

### CUP (`parser.cup`)
**Think:** Understanding grammar rules  
**Does:** Checks if tokens are in correct order  
**Example:** "test" must be followed by a name, then {, then statements, then }

### AST (Abstract Syntax Tree)
**Think:** An outline of your test  
**Does:** Organizes tokens into a tree structure  
**Example:**
```
Program
├── Config (base_url, headers)
├── Variables (id = 42)
└── Test "Login"
    ├── Request: POST /api/login
    └── Assertions: status=200, body contains "token"
```

### Code Generator
**Think:** Translation from outline to full essay  
**Does:** Walks through AST and writes Java code  
**Example:** AST node "POST /api/login" → `HttpRequest.newBuilder().POST(...)`

---

## ⚠️ Error Handling Examples

### Example 1: Missing Semicolon
**You write:**
```
test BadTest {
    GET "/api/test"     ← Missing ;
    expect status = 200;
}
```

**You see:**
```
Parse error at line 2: Syntax error
Expected: SEMICOLON after GET request
```

**Fix:** Add semicolon: `GET "/api/test";`

---

### Example 2: Not Enough Assertions
**You write:**
```
test IncompleteTest {
    GET "/api/test";
    expect status = 200;    ← Only 1 assertion!
}
```

**You see:**
```
Validation error in test 'IncompleteTest':
Test must contain at least 2 assertions (found 1)

Please ensure your tests meet the requirements:
  - At least 1 HTTP request per test
  - At least 2 assertions per test
```

**Fix:** Add another assertion: `expect body contains "data";`

---

### Example 3: Invalid Identifier
**You write:**
```
let 2bad = "value";    ← Can't start with number!
```

**You see:**
```
Lexer error at line 1, column 5: Identifier cannot start with a digit
  Near: '2bad'
```

**Fix:** Start with letter: `let bad2 = "value";`

---

## 📊 Project Statistics

- **DSL Code:** 46 lines (`example.test`)
- **Generated Java:** 65 lines (`GeneratedTests.java`)
- **Ratio:** 1 DSL line → 1.4 Java lines (much simpler!)
- **Test Coverage:** 44 unit tests for parser
- **Exception Types:** 3 custom exceptions
- **Test Success Rate:** 100% (3/3 passing)

---

## 🐛 Troubleshooting

### "Port 8080 already in use"
**Problem:** Another program is using that port  
**Fix:** Either stop the other program, or change port in `example.test`:
```
config {
    base_url = "http://localhost:8081";    ← Change port
}
```

### "Cannot find GeneratedTests"
**Problem:** File wasn't generated or you're in wrong folder  
**Fix:** 
1. Check `GeneratedTests.java` exists: `ls GeneratedTests.java`
2. Make sure you ran Step 3
3. Be in the project root folder

### Tests fail
**Problem:** Backend server isn't running  
**Fix:** Check Terminal Window #1 - server should still be running

### "BUILD FAILURE"
**Problem:** Java or Maven not installed correctly  
**Fix:** Check versions:
```bash
java -version    # Should be 11 or higher
mvn -version     # Should be 3.6 or higher
```

---


## 📚 Complete File Reference

| File | Purpose | Type |
|------|---------|------|
| **DSL Files** |
| `example.test` | Example test suite | TestLang++ DSL |
| `invalid.test` | Error examples | TestLang++ DSL |
| **Scanner/Parser** |
| `lexer.flex` | Tokenizer/Scanner | JFlex |
| `parser.cup` | Grammar/Parser | CUP |
| **Code Generation** |
| `CodeGenerator.java` | AST → Java converter | Java |
| `Program.java` | AST root node | Java |
| `Config.java` | Config AST node | Java |
| `Variable.java` | Variable AST node | Java |
| `Test.java` | Test AST node | Java |
| `Request.java` | Request AST node | Java |
| `Assertion.java` | Assertion AST node | Java |
| `Header.java` | Header AST node | Java |
| **Error Handling** |
| `LexerException.java` | Scanner errors | Java |
| `ParserException.java` | Grammar errors | Java |
| `ValidationException.java` | Validation errors | Java |
| `TestLangParser.java` | Main entry point | Java |
| **Backend** |
| `TestLangDemoApplication.java` | REST API server | Java (Spring Boot) |
| **Generated** |
| `GeneratedTests.java` | JUnit test code | Java (generated) |
| **Tests** |
| `LexerTest.java` | Scanner tests (16) | JUnit 5 |
| `ParserTest.java` | Parser tests (14) | JUnit 5 |
| `CodeGeneratorTest.java` | Generator tests (14) | JUnit 5 |

---

*By IT23632332 - U. T. S. Ranathunga*  
*SE2022 - Programming Paradigms Assignment*  
*TestLang++ DSL Implementation*
