package io.vertx.example.unit.test.local;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.example.web.proxy.SimpleREST;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.example.web.proxy.healthcheck.InMemReporter;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class SimpleRestTest {

    public static final String LOCALHOST = "localhost";
    static DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
    static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new SimpleREST(new InMemReporter(new VerticalServiceRegistry())),
                options,context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testServiceA(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        // Send a request and get a response
        int port = VertxInitUtils.getPort(options);
        client.getNow(port, LOCALHOST, "/serviceA", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceA" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                context.assertTrue(body.toString().contains("prod3568"));
                context.assertTrue(body.toString().contains("prod8643"));
//                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testWhoAmI(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        // Send a request and get a response
        int port = VertxInitUtils.getPort(options);
        client.getNow(port, LOCALHOST, "/whoAmI", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/whoAmI" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(Integer.toString(port)));
                async.complete();
            });
        });
    }

    @Test
    public void testServiceB(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        // Send a request and get a response
        int port = VertxInitUtils.getPort(options);
        client.getNow(port, LOCALHOST, "/serviceB", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceB" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                context.assertTrue(body.toString().contains("prod3568"));
                context.assertTrue(body.toString().contains("prod8643"));
//                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testServiceAProd7340(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        int port = VertxInitUtils.getPort(options);
        client.getNow(port, LOCALHOST, "/serviceA/prod7340", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceA/prod7340" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                async.complete();
//                client.close();
            });
        });
    }

}
