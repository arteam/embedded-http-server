package com.github.arteam;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class TinyHttpServer {

    private HttpServer sunHttpServer;
    private HttpHandler handler;

    public TinyHttpServer(HttpHandler handler) {
        this.handler = handler;
    }

    public void start(int port) {
        try {
            sunHttpServer = HttpServer.create(new InetSocketAddress(port), 50);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sunHttpServer.createContext("/", httpExchange -> {
            try {
                Headers requestHeaders = httpExchange.getRequestHeaders();
                HttpResponse response = handler.handle(new HttpRequest(httpExchange.getRequestMethod(),
                        httpExchange.getRequestURI(), httpExchange.getProtocol(), requestHeaders,
                        readFromStream(httpExchange.getRequestBody())));
                for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
                    httpExchange.getResponseHeaders().put(e.getKey(), e.getValue());
                }
                httpExchange.sendResponseHeaders(response.getStatusCode(), response.getBody().length());
                writeToStream(httpExchange.getResponseBody(), response.getBody());
            } finally {
                httpExchange.close();
            }
        });
        sunHttpServer.start();
    }

    public void stop() {
        sunHttpServer.stop(0);
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
