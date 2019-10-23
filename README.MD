# Result

## Annoying by try&catch code looks like following ?

```java
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.net.URI;

public class Foo{
    private static final int HTTP_TIMEOUT = 5000;

    public static void main(String... args){
        String url = "http://www.baidu.com";

        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .setConnectionManagerShared(true)
                .build();
        CloseableHttpResponse resp = null;
        try {
            URI uriObject = new URI(url);
            HttpGet httpGet = new HttpGet(uriObject);
            httpGet.setConfig(RequestConfig.custom().setConnectTimeout(HTTP_TIMEOUT).build());
            // Create a custom response handler
            resp = httpclient.execute(httpGet);
            System.out.println(new String(getBody(resp)));
        } catch (Exception e) {
            try {
                httpclient.close();
                resp.close();
            } catch (Exception ignored) {
            }
            throw new RuntimeException("get " + url + " fail");
        }
    }
    private static byte[] getBody(final HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        if (status < 200 || status >= 300) {
            return null;
        }
        try {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toByteArray(entity) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
```

## Avoid try-catch by Result api!

```java
package com.salpadding.exceptional;

import org.apache.http.HttpException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.net.URI;

public class Bar{
    private static final int HTTP_TIMEOUT = 5000;

    public static void main(String... args) throws RuntimeException{
        String url = "http://www.baidu.com";

        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .setConnectionManagerShared(true)
                .build();

        Result<CloseableHttpClient> client = Result
                .of(httpclient)
                .onClean(Closeable::close);

        Result.supply(() -> new URI(url))
                .map(HttpGet::new)
                .ifPresent(x -> x.setConfig(RequestConfig.custom().setConnectTimeout(HTTP_TIMEOUT).build()))
                .flatMap((req) -> client.map(c -> c.execute(req)))
                .onClean(Closeable::close)
                .map(resp -> {
                    int status = resp.getStatusLine().getStatusCode();
                    if (status < 200 || status >= 300){
                        throw new HttpException(status + " http error");
                    }
                    return resp;
                })
                .map(resp -> EntityUtils.toByteArray(resp.getEntity()))
                .ifPresent(body -> System.out.println(new String(body)))
                .except((e) -> System.err.printf("get %s failed", url))
                .cleanUp()
        ;
    }
}
```

