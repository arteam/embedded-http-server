package com.github.arteam.embedhttp;

import com.github.arteam.embedhttp.junit.EmbeddedHttpServerRule;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Artem Prigoda
 * @since 28.11.16
 */
public class RunsOnSpecificPort {

    @Rule
    public EmbeddedHttpServerRule httpServerRule = new EmbeddedHttpServerRule()
            .withPort(15678)
            .addHandler("/hello", (request, response) -> response.setBody("Hello, World!")
                    .addHeader("content-type", "text/plain"));

    private CloseableHttpClient httpClient = HttpClients.createMinimal();

    @After
    public void tearDown() throws Exception {
        httpClient.close();
    }

    @Test
    public void test() throws Exception {
        HttpGet httpGet = new HttpGet("http://127.0.0.1:15678/hello");
        String response = httpClient.execute(httpGet, httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue()).isEqualTo("text/plain");
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response).isEqualTo("Hello, World!");
    }
}
