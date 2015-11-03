package io.vertx.example.unit.test;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.web.proxy.SimpleREST;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ProxyToRestTest {

    Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(SimpleREST.class.getName(),
                context.asyncAssertSuccess());
        vertx.deployVerticle(ProxyServer.class.getName(),
                context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testServiceA(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        // Send a request and get a response
        client.getNow(ProxyServer.PORT, "localhost", "/serviceA", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceA" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                context.assertTrue(body.toString().contains("prod3568"));
                context.assertTrue(body.toString().contains("prod8643"));
                async.complete();
                client.close();
            });
        });
    }

    @Test
    public void testServiceAProd3568(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(ProxyServer.PORT, "localhost", "/serviceA/prod3568", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceA/prod3568" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(resp.statusCode() == HttpResponseStatus.FORBIDDEN.code());
                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testServiceB(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(ProxyServer.PORT, "localhost", "/serviceB", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceB" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(resp.statusCode() == HttpResponseStatus.FORBIDDEN.code());
                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testServiceAProd7340(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(ProxyServer.PORT, "localhost", "/serviceA/prod7340", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceA/prod7340" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                client.close();
                async.complete();
            });
        });
    }
}
