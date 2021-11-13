package com.github.arteam.embedhttp;

import java.io.IOException;

/**
 * A functional interface which represents a handler for an HTTP request.
 * It provides ability to get access to the parameters of the request and
 * set-up an HTTP response.
 */
@FunctionalInterface
public interface HttpHandler {

    /**
     * Handles an HTTP request and builds an HTTP response.
     */
    void handle(HttpRequest request, HttpResponse response) throws IOException;

}
