package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Headers;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an HTTP request from an HTTP client
 */
public class HttpRequest {

    private final String method;
    private final URI uri;
    private final Map<String, String> queryParameters;
    private final String protocolVersion;
    private final Headers headers;
    private final String body;

    public HttpRequest(String method, URI uri, String protocolVersion, Headers headers, String body) {
        this.method = method;
        this.uri = uri;
        this.protocolVersion = protocolVersion;
        this.headers = headers;
        this.body = body;
        this.queryParameters = queryParameters(uri);
    }

    public String method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public Map<String, String> queryParameters() {
        return queryParameters;
    }

    public String queryParameter(String name) {
        return queryParameters.get(name);
    }

    public String protocolVersion() {
        return protocolVersion;
    }

    public Headers headers() {
        return headers;
    }

    public String header(String key) {
        return headers.getFirst(key);
    }

    public String contentType() {
        return header("Content-Type");
    }

    public String body() {
        return body;
    }

    /**
     * Gets the query parameters from the request body as a {@link Map}. The query parameters are
     * URI-encoded, and we should decode them when populating the map. In case we have several
     * parameters with the same name, the last one wins.
     */
    public Map<String, String> queryParametersFromBody() {
        return toMap(body);
    }

    /**
     * Gets the query parameters from the provided URI as a {@link Map}. The query parameters are
     * URI-encoded, and we should decode them when populating the map. In case we have several
     * parameters with the same name, the last one wins.
     */
    private static Map<String, String> queryParameters(URI uri) {
        return toMap(uri.getRawQuery());
    }

    private static Map<String, String> toMap(String source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(source.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(p -> decodeUrlPart(p[0]), p -> decodeUrlPart(p[1]),
                        (first, second) -> second));
    }

    private static String decodeUrlPart(String encodedPart) {
        return URLDecoder.decode(encodedPart, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "HttpRequest{method='" + method + ", uri=" + uri + ", queryParameters=" + queryParameters +
                ", protocolVersion='" + protocolVersion + ", headers=" + headers.entrySet() + ", body='" + body + "}";
    }
}
