package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class EmbeddedHttpServer implements Closeable {

    private HttpServer sunHttpServer;
    private List<HttpHandlerConfig> handlers = new ArrayList<>();

    public EmbeddedHttpServer addHandler(String path, HttpHandler handler) {
        return addHandler(path, handler, null);
    }

    public EmbeddedHttpServer addHandler(String path, HttpHandler handler, Authenticator authenticator) {
        handlers.add(new HttpHandlerConfig(path, handler, authenticator));
        return this;
    }

    public EmbeddedHttpServer start() {
        return start(0);
    }

    public EmbeddedHttpServer start(int port) {
        return start(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
    }

    public EmbeddedHttpServer start(InetSocketAddress address) {
        try {
            sunHttpServer = HttpServer.create(address, 50);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (HttpHandlerConfig config : handlers) {
            HttpContext context = sunHttpServer.createContext(config.path, httpExchange -> {
                try {
                    Headers requestHeaders = httpExchange.getRequestHeaders();
                    HttpResponse response = new HttpResponse();
                    config.httpHandler.handle(new HttpRequest(httpExchange.getRequestMethod(),
                            httpExchange.getRequestURI(), httpExchange.getProtocol(), requestHeaders,
                            readFromStream(httpExchange.getRequestBody())), response);
                    for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
                        httpExchange.getResponseHeaders().put(e.getKey(), e.getValue());
                    }
                    httpExchange.sendResponseHeaders(response.getStatusCode(), response.getBody().length());
                    writeToStream(httpExchange.getResponseBody(), response.getBody());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpExchange.close();
                }
            });
            if (config.authenticator != null) {
                context.setAuthenticator(config.authenticator);
            }
        }
        sunHttpServer.start();
        return this;
    }

    public void stop() {
        sunHttpServer.stop(0);
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    public int getPort() {
        return sunHttpServer.getAddress().getPort();
    }

    public String getBindHost() {
        return sunHttpServer.getAddress().getHostName();
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            StringBuilder stringBuilder = new StringBuilder();
            CharBuffer readBuffer = CharBuffer.allocate(1024);
            while (reader.read(readBuffer) != -1) {
                readBuffer.flip();
                stringBuilder.append(readBuffer);
                readBuffer.clear();
            }
            return stringBuilder.toString();
        }
    }

    private static void writeToStream(OutputStream outputStream, String result) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write(result);
        }
    }

    private static class HttpHandlerConfig {

        private final String path;
        private final HttpHandler httpHandler;
        private final Authenticator authenticator;

        HttpHandlerConfig(String path, HttpHandler httpHandler, Authenticator authenticator) {
            this.path = path;
            this.httpHandler = httpHandler;
            this.authenticator = authenticator;
        }
    }
}
