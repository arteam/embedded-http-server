package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Authenticator;

class HandlerConfig {

    private final String path;
    private final HttpHandler httpHandler;
    private final Authenticator authenticator;

    HandlerConfig(String path, HttpHandler httpHandler) {
        this(path, httpHandler, null);
    }

    HandlerConfig(String path, HttpHandler httpHandler, Authenticator authenticator) {
        this.path = path;
        this.httpHandler = httpHandler;
        this.authenticator = authenticator;
    }

    public String getPath() {
        return path;
    }

    public HttpHandler getHttpHandler() {
        return httpHandler;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }
}
