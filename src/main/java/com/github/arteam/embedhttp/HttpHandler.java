package com.github.arteam.embedhttp;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
@FunctionalInterface
public interface HttpHandler {

    void handle(HttpRequest request, HttpResponse response);
}
