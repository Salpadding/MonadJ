package com.salpadding.exceptional;

import org.apache.http.HttpException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.net.URI;

public class Bar {
    private static final int HTTP_TIMEOUT = 5000;

    public static void main(String... args) {
        String url = "http://www.baidu.com";

        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager()).setConnectionManagerShared(true)
                .build();

        Monad<CloseableHttpClient, Exception> client = Monad.of(httpclient).onClean(Closeable::close); // clean

        String responseBody = Monad.of(url).map(URI::new).map(HttpGet::new)
                .ifPresent(x -> x.setConfig(RequestConfig.custom().setConnectTimeout(HTTP_TIMEOUT).build()))
                .compose(client, (g, c) -> c.execute(g))
                .onClean(Closeable::close) // register cleaner
                .map(resp -> {
                    int status = resp.getStatusLine().getStatusCode();
                    // throw exception when http error occurs
                    if (status < 200 || status >= 300) {
                        throw new HttpException(status + " http error");
                    }
                    return resp;
                }).map(CloseableHttpResponse::getEntity).map(EntityUtils::toByteArray)
                .map(String::new)
                .cleanUp()
                .get(t -> new RuntimeException("request " + url + " failed"));
        System.out.println("the length of response body is " + responseBody.length());
        ;
    }
}