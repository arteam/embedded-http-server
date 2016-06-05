package com.github.arteam;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class TinyHttpServer {

    private HttpServer sunHttpServer;
    private Map<String, HttpHandler> handlers = new HashMap<>();

    public TinyHttpServer addHandler(String path, HttpHandler handler) {
        if (handlers.put(path, handler) != null) {
            throw new IllegalArgumentException("Handler on path=" + path + " has been already added");
        }
        return this;
    }

    public TinyHttpServer start() {
        return start(0);
    }

    public TinyHttpServer start(int port) {
        return start(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
    }

    public TinyHttpServer start(InetSocketAddress address) {
        try {
            sunHttpServer = HttpServer.create(address, 50);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, HttpHandler> ph : handlers.entrySet()) {
            String path = ph.getKey();
            HttpHandler httpHandler = ph.getValue();
            sunHttpServer.createContext(path, httpExchange -> {
                try {
                    Headers requestHeaders = httpExchange.getRequestHeaders();
                    HttpResponse response = new HttpResponse();
                    httpHandler.handle(new HttpRequest(httpExchange.getRequestMethod(),
                            httpExchange.getRequestURI(), httpExchange.getProtocol(), requestHeaders,
                            readFromStream(httpExchange.getRequestBody())), response);
                    for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
                        httpExchange.getResponseHeaders().put(e.getKey(), e.getValue());
                    }
                    httpExchange.sendResponseHeaders(response.getStatusCode(), response.getBody().length());
                    writeToStream(httpExchange.getResponseBody(), response.getBody());
                } finally {
                    httpExchange.close();
                }
            });
        }
        sunHttpServer.start();
        return this;
    }

    public void stop() {
        sunHttpServer.stop(0);
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
}
