package com.testlang.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
@RestController
public class TestLangDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestLangDemoApplication.class, args);
    }

    /**
     * POST /api/login - Login endpoint
     * Accepts JSON with username and password
     * Returns JSON with token
     */
    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-App", "TestLangDemo");

        Map<String, Object> response = new HashMap<>();
        response.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test");
        response.put("username", credentials.get("username"));
        response.put("success", true);

        return ResponseEntity.ok().headers(headers).body(response);
    }

    /**
     * GET /api/users/{id} - Get user by ID
     * Returns JSON with user info
     */
    @GetMapping("/api/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-App", "TestLangDemo");

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("username", "user" + id);
        response.put("email", "user" + id + "@example.com");
        response.put("role", "USER");

        return ResponseEntity.ok().headers(headers).body(response);
    }

    /**
     * PUT /api/users/{id} - Update user
     * Accepts JSON with updated fields
     * Returns JSON with update confirmation
     */
    @PutMapping("/api/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable int id,
            @RequestBody Map<String, String> updates) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-App", "TestLangDemo");

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("updated", true);
        response.put("role", updates.get("role"));
        response.put("message", "User " + id + " updated successfully");

        return ResponseEntity.ok().headers(headers).body(response);
    }
}
