package com.github.arteam.embedhttp;

import com.github.arteam.embedhttp.junit.EmbeddedHttpServerExtension;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.InetSocketAddress;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RunsOnSpecificAddressTest {

    @RegisterExtension
    public EmbeddedHttpServerExtension httpServerExtension = new EmbeddedHttpServerExtension()
            .address(new InetSocketAddress("0.0.0.0", 13456))
            .handler("/bye", (request, response) -> response
                    .body("Bye, bye.")
                    .header("content-type", "text/plain"));

    @RegisterExtension
    public HttpClientExtension httpClientExtension = new HttpClientExtension();

    @Test
    void test() throws Exception {
        HttpGet httpGet = new HttpGet("http://127.0.0.1:13456/bye");
        String response = httpClientExtension.get().execute(httpGet, httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue()).isEqualTo("text/plain");
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response).isEqualTo("Bye, bye.");
    }
}
