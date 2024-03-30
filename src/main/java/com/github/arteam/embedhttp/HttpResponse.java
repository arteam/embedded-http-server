package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Headers;

/**
 * Represents an HTTP responses from an HTTP server
 */
public class HttpResponse {

    private static final int STATUS_CODE_OK = 200;

    private int statusCode;
    private Headers headers;
    private String body;

    public HttpResponse() {
        this(STATUS_CODE_OK, new Headers(), "");
    }

    public HttpResponse(int statusCode, Headers headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpResponse statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    Headers headers() {
        return headers;
    }

    public HttpResponse header(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public HttpResponse headers(Headers headers) {
        this.headers = headers;
        return this;
    }

    public String body() {
        return body;
    }

    public HttpResponse body(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers.entrySet() +
                ", body='" + body + '\'' +
                '}';
    }
}
