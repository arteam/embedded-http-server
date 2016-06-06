package com.github.arteam.tinyhttp;

import com.github.arteam.tinyhttp.TinyHttpServer;
import com.sun.net.httpserver.BasicAuthenticator;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
        }).addHandler("/search", (request, response) -> {
            System.out.println(request);
            assertThat(request.getQueryParameter("name"), CoreMatchers.equalTo("Andr&as"));
            assertThat(request.getQueryParameter("city"), CoreMatchers.equalTo("H=mburg"));
            response.setBody("No Andreas in Hamburg")
                    .addHeader("content-type", "text/plain");
        }).addHandler("/post", (request, response) -> {
            System.out.println(request);
            assertThat(request.getContentType(), CoreMatchers.equalTo("application/json; charset=UTF-8"));
            response.setBody("{\"message\": \"Roger that!\"}")
                    .addHeader("content-type", "application/json");
        }).addHandler("/protected", (request, response) -> {
            System.out.println(request);
            assertThat(request.getContentType(), CoreMatchers.equalTo("application/json; charset=UTF-8"));
            response.setBody("{\"message\": \"Roger admin!\"}")
                    .addHeader("content-type", "application/json");
        }, new BasicAuthenticator("tiny-http-server") {
            @Override
            public boolean checkCredentials(String username, String password) {
                return username.equals("scott") && password.equals("tiger");
            }
        }).start();
        System.out.println("Server port is: " + httpServer.getPort());
        System.out.println("Server host is: " + httpServer.getBindHost());
    }

    @After
    public void tearDown() throws Exception {
        httpClient.close();
        httpServer.stop();
    }

    @Test
    public void testHelloWorld() throws Exception {
        assertGetHelloWorld(httpClient);
    }

    @Test
    public void testSeveralHelloWorlds() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertGetHelloWorld(httpClient);
        }
    }

    private void assertGetHelloWorld(CloseableHttpClient httpClient) throws java.io.IOException {
        HttpGet httpGet = new HttpGet(String.format("http://127.0.0.1:%s/get", httpServer.getPort()));
        String response = httpClient.execute(httpGet, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue(), CoreMatchers.equalTo("text/plain"));
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response, CoreMatchers.equalTo("Hello, World!"));
    }

    @Test
    public void testPost() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/post", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"Hello, World!\"}", ContentType.APPLICATION_JSON));
        String response = httpClient.execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue(), CoreMatchers.equalTo("application/json"));
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response, CoreMatchers.equalTo("{\"message\": \"Roger that!\"}"));
    }

    @Test
    public void testAuth() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/protected", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"I am the admin!\"}", ContentType.APPLICATION_JSON));
        httpPost.addHeader(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString("scott:tiger".getBytes(StandardCharsets.UTF_8)));
        String response = httpClient.execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue(), CoreMatchers.equalTo("application/json"));
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response, CoreMatchers.equalTo("{\"message\": \"Roger admin!\"}"));
    }

    @Test
    public void testNotAuthorized() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/protected", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"I am the admin!\"}", ContentType.APPLICATION_JSON));
        httpPost.addHeader(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString("bill:wolf".getBytes(StandardCharsets.UTF_8)));
        httpClient.execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode(), CoreMatchers.equalTo(401));
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
    }

    @Test
    public void testQueryParameters() throws Exception {
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("127.0.0.1")
                .setPort(httpServer.getPort())
                .setPath("/search")
                .addParameter("name", "Andr&as")
                .addParameter("city", "H=mburg")
                .build();
        String response = httpClient.execute(new HttpGet(uri), httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode(), CoreMatchers.equalTo(200));
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response, CoreMatchers.equalTo("No Andreas in Hamburg"));
    }
}