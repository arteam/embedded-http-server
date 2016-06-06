package com.github.arteam.tinyhttp;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
@FunctionalInterface
public interface HttpHandler {

    void handle(HttpRequest request, HttpResponse response);
}
