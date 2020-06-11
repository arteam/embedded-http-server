package com.github.arteam.embedhttp.junit;

import com.github.arteam.embedhttp.EmbeddedHttpServer;
import com.github.arteam.embedhttp.HttpHandler;
import com.sun.net.httpserver.Authenticator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * A JUnit rule for starting up an HTTP server in tests.
 */
public class EmbeddedHttpServerExtension implements BeforeEachCallback, AfterEachCallback {

    private EmbeddedHttpServer embeddedHttpServer = new EmbeddedHttpServer();
    private InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);

    /**
     * Adds a new handler to the server to a path.
     */
    public EmbeddedHttpServerExtension addHandler(String path, HttpHandler handler) {
        return addHandler(path, handler, null);
    }

    /**
     * Adds a new handler to the server to a path with an authenticator.
     */
    public EmbeddedHttpServerExtension addHandler(String path, HttpHandler handler, Authenticator authenticator) {
        embeddedHttpServer.addHandler(path, handler, authenticator);
        return this;
    }

    /**
     * Sets a port on which the server should start up
     */
    public EmbeddedHttpServerExtension withPort(int port) {
        return withAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
    }

    /**
     * Sets an address on which the server should start up
     */
    public EmbeddedHttpServerExtension withAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
        return this;
    }

    /**
     * Gets the port on which server has been started
     */
    public int getPort() {
        return embeddedHttpServer.getPort();
    }

    /**
     * Gets the host on which server has been bound
     */
    public String getBindHost() {
        return embeddedHttpServer.getBindHost();
    }


    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        embeddedHttpServer.start(inetSocketAddress);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        embeddedHttpServer.stop();
    }

}
