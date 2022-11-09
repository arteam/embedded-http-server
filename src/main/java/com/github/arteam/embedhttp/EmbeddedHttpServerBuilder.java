package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Authenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class EmbeddedHttpServerBuilder {


    /**
     * Creates a new builder to generate the server
     * @return builder
     */
    public static EmbeddedHttpServerBuilder createNew() {
        return new EmbeddedHttpServerBuilder();
    }

    private int port = 8080;
    private List<HandlerConfig> handlers = new ArrayList<>();
    private Executor executor;

    /**
     * Nobody should use this
     */
    private EmbeddedHttpServerBuilder() {}

    /**
     * Use the server with this port
     * @param port Value of port, by default 8080
     * @return Builder
     */
    EmbeddedHttpServerBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Add a handler with an autenticator for the server
     * @param path Path to handle
     * @param handler Functional method to execute the handling
     * @param authenticator to authenticate user
     * @return
     */
    EmbeddedHttpServerBuilder addHandler(String path,Authenticator authenticator, HttpHandler handler) {
        this.handlers.add(new HandlerConfig(path, handler, authenticator));
        return this;
    }

    /**
     * Add a handler for the server
     * @param path Path to handle
     * @param handler Functional method to execute the handling
     * @return Builder
     */
    EmbeddedHttpServerBuilder addHandler(String path, HttpHandler handler) {
        this.handlers.add(new HandlerConfig(path, handler));
        return this;
    }

    /**
     * Adds a custom executor for the server
     * @param executor Executor to add
     * @return Builder
     */
    EmbeddedHttpServerBuilder addExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Finally, build the server
     * The server should be running when retunrning
     * @return Started server
     */
    EmbeddedHttpServer buildAndRun() {
        EmbeddedHttpServer embeddedHttpServer = new EmbeddedHttpServer();
        embeddedHttpServer.addHandlers(handlers);
        if (executor != null) {
            embeddedHttpServer.addExecutor(executor);
        }
        embeddedHttpServer.start(port);
        return embeddedHttpServer;
    }


}
