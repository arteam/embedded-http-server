package com.github.arteam.embedhttp;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class HttpClientExtension implements AfterEachCallback {

    private final CloseableHttpClient httpClient = HttpClients.createMinimal();

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        httpClient.close();
    }

    public CloseableHttpClient get() {
        return httpClient;
    }
}
