package com.testlang.ast;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents an HTTP request (GET/POST/PUT/DELETE)
 */
public class Request implements Statement {
    private String method; // GET, POST, PUT, DELETE
    private String url;
    private List<Header> headers;
    private String body;

    public Request(String method, String url) {
        this.method = method;
        this.url = url;
        this.headers = new ArrayList<>();
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void addHeader(Header header) {
        this.headers.add(header);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
