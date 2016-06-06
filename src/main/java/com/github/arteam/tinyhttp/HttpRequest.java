package com.github.arteam.tinyhttp;

import com.sun.net.httpserver.Headers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Artem Prigoda
 * @since 05.06.16
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
        this.queryParameters = getQueryParameters(uri);
    }

    public String getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getQueryParameter(String name) {
        return queryParameters.get(name);
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getFirstHeader(String key) {
        return headers.getFirst(key);
    }

    public String getContentType() {
        return getFirstHeader("Content-Type");
    }

    public String getBody() {
        return body;
    }

    /**
     * Gets query parameters from the provided URI as a {@link Map}. The query parameters are
     * URI-encoded, and we should decode them when populating the map. In case we have several
     * parameters with the same name, the last one wins.
     */
    private Map<String, String> getQueryParameters(URI uri) {
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return Collections.emptyMap();
        }
        return Stream.of(rawQuery.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(p -> decodeUrlPart(p[0]), p -> decodeUrlPart(p[1]), (first, second) -> second));
    }

    private static String decodeUrlPart(String encodedPart) {
        try {
            return URLDecoder.decode(encodedPart, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        return "HttpRequest{" + "method='" + method + '\'' +
                ", uri=" + uri +
                ", queryParameters=" + queryParameters +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", headers=" + headers.entrySet() +
                ", body='" + body + '\'' +
                '}';
    }
}
