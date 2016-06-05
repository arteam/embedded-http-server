package com.github.arteam;

import com.sun.net.httpserver.Headers;

import java.net.URI;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class HttpRequest {

    private final String method;
    private final URI uri;
    private final String protocolVersion;
    private final Headers headers;
    private final String body;

    public HttpRequest(String method, URI uri, String protocolVersion, Headers headers, String body) {
        this.method = method;
        this.uri = uri;
        this.protocolVersion = protocolVersion;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String toString() {
        return "HttpRequest{" + "method='" + method + '\'' +
                ", uri=" + uri +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", headers=" + headers.entrySet() +
                ", body='" + body + '\'' +
                '}';
    }
}
