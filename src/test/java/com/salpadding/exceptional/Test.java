package com.salpadding.exceptional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Closeable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RunWith(JUnit4.class)
public class Test {

    private static final int HTTP_TIMEOUT = 5000;
    private static final Executor executor = command -> new Thread(command).start();

    @org.junit.Test
    public void test1() {
        getAsync("http://www.baidu.com", new HashMap<>())
                .thenAccept((body) -> System.out.println(new String(body))).join();
    }

    private static CompletableFuture<byte[]> getAsync(final String url, Map<String, String> query) {
        return CompletableFuture.supplyAsync(() -> get(url, query), executor);
    }

    private static byte[] get(final String url, Map<String, String> query) throws RuntimeException {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager()).setConnectionManagerShared(true)
                .build();

        return Result.supply(() -> {
            URI uriObject = new URI(url);
            URIBuilder builder = new URIBuilder().setScheme(uriObject.getScheme()).setHost(uriObject.getHost())
                    .setPort(uriObject.getPort()).setPath(uriObject.getPath());
            for (String k : query.keySet()) {
                builder.setParameter(k, query.get(k));
            }
            return builder.build();
        }).map(HttpGet::new)
                .flatMap(req -> Result.completedResult(httpclient).onClean((c) -> {
                    c.close();
                    System.out.println("closed");
                }).map((c) -> c.execute(req)))
                .onClean(Closeable::close).map(Test::getBody).cleanUp().get();
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
