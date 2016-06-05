package com.github.arteam;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
@FunctionalInterface
public interface HttpHandler {

    void handle(HttpRequest request, HttpResponse response);
}
