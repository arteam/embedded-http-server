package com.github.arteam.embedhttp;

import com.github.arteam.embedhttp.junit.EmbeddedHttpServerRule;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertThat;

/**
 * @author Artem Prigoda
 * @since 28.11.16
 */
public class RunsOnSpecificAddress {

    @Rule
    public EmbeddedHttpServerRule httpServerRule = new EmbeddedHttpServerRule()
            .withAddress(new InetSocketAddress("0.0.0.0", 13456))
            .addHandler("/bye", (request, response) -> response.setBody("Bye, bye.")
                    .addHeader("content-type", "text/plain"));

    private CloseableHttpClient httpClient = HttpClients.createMinimal();

    @After
    public void tearDown() throws Exception {
        httpClient.close();
    }

    @Test
    public void test() throws Exception {
        HttpGet httpGet = new HttpGet("http://127.0.0.1:13456/bye");
        String response = httpClient.execute(httpGet, httpResponse -> {
            assertThat(httpResponse.getStatusLine().getStatusCode(), CoreMatchers.equalTo(200));
            assertThat(httpResponse.getFirstHeader("Content-Type").getValue(), CoreMatchers.equalTo("text/plain"));
            return EntityUtils.toString(httpResponse.getEntity());
        });
        assertThat(response, CoreMatchers.equalTo("Bye, bye."));
    }
}
