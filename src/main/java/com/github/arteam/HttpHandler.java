package com.github.arteam;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
@FunctionalInterface
public interface HttpHandler {

    HttpResponse handle(HttpRequest request);
}
