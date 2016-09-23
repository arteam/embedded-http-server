package com.github.arteam.embedhttp;

import com.github.arteam.embedhttp.junit.EmbeddedHttpServerRule;
import com.sun.net.httpserver.BasicAuthenticator;
import org.apache.http.HttpHeaders;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static org.junit.Assert.assertThat;

/**
 * @author Artem Prigoda
 * @since 05.06.16
 */
public class EmbeddedHttpServerTest {

    @Rule
    public EmbeddedHttpServerRule httpServer = new EmbeddedHttpServerRule()
            .addHandler("/get", (request, response) -> {
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
                if (!request.getContentType().equals("application/json; charset=UTF-8")) {
                    response.setStatusCode(400);
                    return;
                }
                response.setBody("{\"message\": \"Roger that!\"}")
                        .addHeader("content-type", "application/json");
            })
            .addHandler("/error", ((request, response) -> response.setStatusCode(500)))
            .addHandler("/protected", (request, response) -> {
                System.out.println(request);
                assertThat(request.getContentType(), CoreMatchers.equalTo("application/json; charset=UTF-8"));
                response.setBody("{\"message\": \"Roger admin!\"}")
                        .addHeader("content-type", "application/json");
            }, new BasicAuthenticator("tiny-http-server") {
                @Override
                public boolean checkCredentials(String username, String password) {
                    return username.equals("scott") && password.equals("tiger");
                }
            });

    private CloseableHttpClient httpClient = HttpClients.createMinimal();

    @Before
    public void setUp() throws Exception {
        System.out.println("Server port is: " + httpServer.getPort());
        System.out.println("Server host is: " + httpServer.getBindHost());
    }

    @After
    public void tearDown() throws Exception {
        httpClient.close();
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
    public void testBadRequest() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/post", httpServer.getPort()));
        httpPost.setEntity(new UrlEncodedFormEntity(Collections.singletonList(
                new BasicNameValuePair("greeting", "Hello, World!"))));
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
            assertThat(httpResponse.getStatusLine().getStatusCode(), CoreMatchers.equalTo(400));
        }
    }

    @Test
    public void testWrongPath() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/dead_letter", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"Hello, World!\"}", ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
            assertThat(httpResponse.getStatusLine().getStatusCode(), CoreMatchers.equalTo(404));
        }
    }

    @Test
    public void testServerError() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/error", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"Hello, World!\"}", ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
            assertThat(httpResponse.getStatusLine().getStatusCode(), CoreMatchers.equalTo(500));
        }
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