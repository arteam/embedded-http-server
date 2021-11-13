package com.github.arteam.embedhttp;

import com.github.arteam.embedhttp.junit.EmbeddedHttpServerExtension;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import static com.github.arteam.embedhttp.junit.EmbeddedHttpServerExtension.loadResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class EmbeddedHttpServerTest {

    @RegisterExtension
    public EmbeddedHttpServerExtension httpServer = new EmbeddedHttpServerExtension()
            .addHandler("/get", (request, response) -> {
                System.out.println(request);
                response.setBody("Hello, World!").addHeader("content-type", "text/plain");
            }).addHandler("/search", (request, response) -> {
                System.out.println(request);
                assertThat(request.getQueryParameter("name")).isEqualTo("Andr&as");
                assertThat(request.getQueryParameter("city")).isEqualTo("H=mburg");
                response.setBody("No Andreas in Hamburg").addHeader("content-type", "text/plain");
            }).addHandler("/post", (request, response) -> {
                System.out.println(request);
                if (!request.getContentType().equals("application/json; charset=UTF-8")) {
                    response.setStatusCode(400);
                    return;
                }
                assertThat(request.getBody()).isEqualTo("{\"name\":\"Hello, World!\"}");
                response.setBody(loadResource("/roger_that.json"))
                        .addHeader("content-type", "application/json");
            }).addHandler("/post-parameters", (request, response) -> {
                System.out.println(request);
                if (!request.getContentType().equals("application/x-www-form-urlencoded; charset=UTF-8")) {
                    response.setStatusCode(400);
                    return;
                }
                assertThat(request.getQueryParametersFromBody()).containsOnly(entry("name", "Andr&as"), entry("city", "H=mburg"));
                response.setBody(loadResource("/roger_that.json")).addHeader("content-type", "application/json");
            }).addHandler("/error", ((request, response) -> {
                response.setStatusCode(500);
            })).addHandler("/protected", (request, response) -> {
                System.out.println(request);
                assertThat(request.getContentType()).isEqualTo("application/json; charset=UTF-8");
                response.setBody(loadResource("/roger_admin.json")).addHeader("content-type", "application/json");
            }, new BasicAuthenticator("tiny-http-server") {
                @Override
                public boolean checkCredentials(String username, String password) {
                    return username.equals("scott") && password.equals("tiger");
                }
            });

    @RegisterExtension
    public HttpClientExtension httpClientExtension = new HttpClientExtension();

    @BeforeEach
    void setUp() throws Exception {
        System.out.println("Server port is: " + httpServer.getPort());
        System.out.println("Server host is: " + httpServer.getBindHost());
    }


    @Test
    void testHelloWorld() throws Exception {
        assertGetHelloWorld(httpClientExtension.get());
    }

    @Test
    void testSeveralHelloWorlds() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertGetHelloWorld(httpClientExtension.get());
        }
    }

    private void assertGetHelloWorld(CloseableHttpClient httpClient) throws java.io.IOException {
        HttpGet httpGet = new HttpGet(String.format("http://127.0.0.1:%s/get", httpServer.getPort()));
        String response = httpClient.execute(httpGet, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue()).isEqualTo("text/plain");
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response).isEqualTo("Hello, World!");
    }

    @Test
    void testPost() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/post", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"Hello, World!\"}", ContentType.APPLICATION_JSON));
        String response = httpClientExtension.get().execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response).isEqualTo(loadResource("/roger_that.json"));
    }

    @Test
    void testPostEncodedParameters() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/post-parameters", httpServer.getPort()));
        httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("name", "Andr&as"),
                new BasicNameValuePair("city", "H=mburg")),
                StandardCharsets.UTF_8));
        String response = httpClientExtension.get().execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response).isEqualTo(loadResource("/roger_that.json"));
    }

    @Test
    void testBadRequest() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/post", httpServer.getPort()));
        httpPost.setEntity(new UrlEncodedFormEntity(Collections.singletonList(
                new BasicNameValuePair("greeting", "Hello, World!"))));
        try (CloseableHttpResponse httpResponse = httpClientExtension.get().execute(httpPost)) {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(400);
        }
    }

    @Test
    void testWrongPath() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/dead_letter", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"Hello, World!\"}", ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse httpResponse = httpClientExtension.get().execute(httpPost)) {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(404);
        }
    }

    @Test
    void testServerError() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/error", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"Hello, World!\"}", ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse httpResponse = httpClientExtension.get().execute(httpPost)) {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(500);
        }
    }

    @Test
    void testAuth() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/protected", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"I am the admin!\"}", ContentType.APPLICATION_JSON));
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                Base64.getEncoder().encodeToString("scott:tiger".getBytes(StandardCharsets.UTF_8)));
        String response = httpClientExtension.get().execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue()).isEqualTo("application/json");
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response).isEqualTo(loadResource("/roger_admin.json"));
    }

    @Test
    void testNotAuthorized() throws Exception {
        HttpPost httpPost = new HttpPost(String.format("http://127.0.0.1:%s/protected", httpServer.getPort()));
        httpPost.setEntity(new StringEntity("{\"name\":\"I am the admin!\"}", ContentType.APPLICATION_JSON));
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                Base64.getEncoder().encodeToString("bill:wolf".getBytes(StandardCharsets.UTF_8)));
        httpClientExtension.get().execute(httpPost, httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(401);
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
    }

    @Test
    void testQueryParameters() throws Exception {
        URI uri = new URIBuilder().setScheme("http")
                .setHost("127.0.0.1")
                .setPort(httpServer.getPort())
                .setPath("/search")
                .addParameter("name", "Andr&as")
                .addParameter("city", "H=mburg")
                .build();
        String response = httpClientExtension.get().execute(new HttpGet(uri), httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        });
        assertThat(response).isEqualTo("No Andreas in Hamburg");
    }
}