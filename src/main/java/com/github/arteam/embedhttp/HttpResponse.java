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

    public HttpResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    Headers getHeaders() {
        return headers;
    }

    public HttpResponse addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public HttpResponse setHeaders(Headers headers) {
        this.headers = headers;
        return this;
    }

    public String getBody() {
        return body;
    }

    public HttpResponse setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
