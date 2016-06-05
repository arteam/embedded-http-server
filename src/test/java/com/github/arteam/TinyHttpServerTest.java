package com.github.arteam;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertThat;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class TinyHttpServerTest {

    private TinyHttpServer httpServer;
    private CloseableHttpClient httpClient = HttpClients.createMinimal();

    @Before
    public void setUp() throws Exception {
        httpServer = new TinyHttpServer().addHandler("/get", (request, response) -> {
            System.out.println(request);
            response.setBody("Hello, World!")
                    .addHeader("content-type", "text/plain");
        }).addHandler("/post", (request, response) -> {
            System.out.println(request);
            System.out.println(request.getFirstHeader("content-type"));
            response.setBody("{\"message\": \"Roger that!\"}")
                    .addHeader("content-type", "application/json");
        }).start(8080);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(10000);
        httpClient.close();
        httpServer.stop();
    }

    @Test
    public void testHelloWorld() throws Exception {
        assertGetHelloWorld(httpClient);
    }

    @Test
    public void testSeveralHelloWorlds() throws Exception {
        for (int i = 0; i < 100; i++) {
            assertGetHelloWorld(httpClient);
        }
    }

    private void assertGetHelloWorld(CloseableHttpClient httpClient) throws java.io.IOException {
        String response = httpClient.execute(new HttpGet("http://127.0.0.1:8080"), httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue(), CoreMatchers.equalTo("text/plain"));
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response, CoreMatchers.equalTo("Hello, World!"));
    }

    @Test
    public void testPost() throws Exception {
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/post");
        httpPost.setEntity(new StringEntity("{\"name\":\"Hello, World!\"}", ContentType.APPLICATION_JSON));
        String response = httpClient.execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue(), CoreMatchers.equalTo("application/json"));
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response, CoreMatchers.equalTo("{\"message\": \"Roger that!\"}"));
    }
}