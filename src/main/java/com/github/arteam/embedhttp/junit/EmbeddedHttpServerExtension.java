package com.github.arteam.embedhttp.junit;

import com.github.arteam.embedhttp.EmbeddedHttpServer;
import com.github.arteam.embedhttp.HttpHandler;
import com.sun.net.httpserver.Authenticator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

/**
 * A JUnit rule for starting up an HTTP server in tests.
 */
public class EmbeddedHttpServerExtension implements BeforeEachCallback, AfterEachCallback {

    private final EmbeddedHttpServer embeddedHttpServer = new EmbeddedHttpServer();
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

    /**
     * Loads a resource from the classpath by its full path
     *
     * @param resourcePath the path to the resource starting from `/`
     * @return the resource as a string
     */
    public static String loadResource(String resourcePath) {
        try (InputStream is = requireNonNull(EmbeddedHttpServerExtension.class.getResourceAsStream(resourcePath),
                "Unable to find resource at " + resourcePath)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read resource at " + resourcePath, e);
        }
    }
}
