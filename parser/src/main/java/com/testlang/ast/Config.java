package com.testlang.ast;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the optional config block
 */
public class Config {
    private String baseUrl;
    private List<Header> headers;

    public Config() {
        this.headers = new ArrayList<>();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void addHeader(Header header) {
        this.headers.add(header);
    }
}
