package com.github.arteam.embedhttp.junit;

import com.github.arteam.embedhttp.EmbeddedHttpServer;
import com.github.arteam.embedhttp.HttpHandler;
import com.sun.net.httpserver.Authenticator;
import org.junit.rules.ExternalResource;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * A JUnit rule for starting up an HTTP server in tests.
 *
 * @author Artem Prigoda
 * @since 23.09.16
 */
public class EmbeddedHttpServerRule extends ExternalResource {

    private EmbeddedHttpServer embeddedHttpServer = new EmbeddedHttpServer();
    private InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);

    /**
     * Adds a new handler to the server to a path.
     */
    public EmbeddedHttpServerRule addHandler(String path, HttpHandler handler) {
        return addHandler(path, handler, null);
    }

    /**
     * Adds a new handler to the server to a path with an authenticator.
     */
    public EmbeddedHttpServerRule addHandler(String path, HttpHandler handler, Authenticator authenticator) {
        embeddedHttpServer.addHandler(path, handler, authenticator);
        return this;
    }

    /**
     * Sets a port on which the server should start up
     */
    public EmbeddedHttpServerRule withPort(int port) {
        return withAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
    }

    /**
     * Sets an address on which the server should start up
     */
    public EmbeddedHttpServerRule withAddress(InetSocketAddress inetSocketAddress) {
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
    protected void before() throws Throwable {
        embeddedHttpServer.start(inetSocketAddress);
    }

    @Override
    protected void after() {
        embeddedHttpServer.stop();
    }
}
