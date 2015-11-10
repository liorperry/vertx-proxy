package io.vertx.example.unit.test.local;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.SimpleREST;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SimpleRestTest {

    public static final int REST_PORT = 8082;
    public static final String LOCALHOST = "localhost";
    static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new SimpleREST((result, domain, descriptor) -> HealthCheck.Result.healthy()),
                new DeploymentOptions().setConfig(
                        new JsonObject().put(VertxInitUtils.HTTP_PORT, REST_PORT)), context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testServiceA(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        // Send a request and get a response
        client.getNow(REST_PORT, LOCALHOST, "/serviceA", resp -> {
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
    public void testWhoAmI(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        // Send a request and get a response
        client.getNow(REST_PORT, LOCALHOST, "/whoAmI", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/whoAmI" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(Integer.toString(REST_PORT)));
                async.complete();
                client.close();
            });
        });
    }

    @Test
    public void testServiceB(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        // Send a request and get a response
        client.getNow(REST_PORT, LOCALHOST, "/serviceB", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceB" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                context.assertTrue(body.toString().contains("prod3568"));
                context.assertTrue(body.toString().contains("prod8643"));
                async.complete();
                client.close();
            });
        });
    }

    @Test
    public void testServiceAProd7340(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(REST_PORT, LOCALHOST, "/serviceA/prod7340", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceA/prod7340" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                client.close();
                async.complete();
            });
        });
    }

}
