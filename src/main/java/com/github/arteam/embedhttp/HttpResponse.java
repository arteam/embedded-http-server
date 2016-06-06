package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Headers;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class HttpResponse {

    private int statusCode;
    private Headers headers;
    private String body;

    public HttpResponse() {
        this(200, new Headers(), "");
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

    public Headers getHeaders() {
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
        return "HttpResponse{" + "statusCode=" + statusCode +
                ", headers=" + headers.entrySet() +
                ", body='" + body + '\'' +
                '}';
    }
}
