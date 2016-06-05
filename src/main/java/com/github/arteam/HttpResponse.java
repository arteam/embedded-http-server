package com.github.arteam;

import com.sun.net.httpserver.Headers;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class HttpResponse {

    private final int statusCode;
    private final Headers headers;
    private final String body;

    public HttpResponse(int statusCode, Headers headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public HttpResponse(String body) {
        this(200, new Headers(), body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpResponse{" + "statusCode=" + statusCode +
                ", headers=" + headers.entrySet() +
                ", body='" + body + '\'' +
                '}';
    }
}
