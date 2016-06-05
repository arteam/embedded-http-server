package com.github.arteam;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class TinyHttpServerTest {

    TinyHttpServer httpServer;

    @Before
    public void setUp() throws Exception {
        httpServer = new TinyHttpServer((request, response) -> {
            System.out.println(request);
            response.setBody("Hello, World!")
                    .addHeader("Content-Type", "text/plain");
        });
        httpServer.start(8080);
    }

    @After
    public void tearDown() throws Exception {
        httpServer.stop();
    }

    @Test
    public void testHelloWorld() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
            assertGetHelloWorld(httpClient);
        }
    }

    @Test
    public void testSeveralHelloWorlds() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createMinimal()) {
            for (int i = 0; i < 100; i++) {
                assertGetHelloWorld(httpClient);
            }
        }
    }

    private void assertGetHelloWorld(CloseableHttpClient httpClient) throws java.io.IOException {
        String response = httpClient.execute(new HttpGet("http://127.0.0.1:8080"), httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue(), CoreMatchers.equalTo("text/plain"));
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response, CoreMatchers.equalTo("Hello, World!"));
    }
}