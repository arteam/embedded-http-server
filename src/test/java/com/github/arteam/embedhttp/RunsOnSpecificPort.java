package com.github.arteam.embedhttp;

import com.github.arteam.embedhttp.junit.EmbeddedHttpServerExtension;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class RunsOnSpecificPort {

    @RegisterExtension
    public EmbeddedHttpServerExtension httpServerExtension = new EmbeddedHttpServerExtension()
            .withPort(15678)
            .addHandler("/hello", (request, response) -> response.setBody("Hello, World!")
                    .addHeader("content-type", "text/plain"));

    private CloseableHttpClient httpClient = HttpClients.createMinimal();

    @AfterEach
    void tearDown() throws Exception {
        httpClient.close();
    }

    @Test
    void test() throws Exception {
        HttpGet httpGet = new HttpGet("http://127.0.0.1:15678/hello");
        String response = httpClient.execute(httpGet, httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue()).isEqualTo("text/plain");
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response).isEqualTo("Hello, World!");
    }
}
