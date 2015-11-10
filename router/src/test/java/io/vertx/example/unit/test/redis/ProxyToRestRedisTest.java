package io.vertx.example.unit.test.redis;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.web.proxy.RedisStarted;
import io.vertx.example.web.proxy.SimpleREST;
import io.vertx.example.web.proxy.filter.Filter;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.healthcheck.RedisReporter;
import io.vertx.example.web.proxy.locator.RedisServiceLocator;
import io.vertx.example.web.proxy.repository.KeysRepository;
import io.vertx.example.web.proxy.repository.RedisKeysRepository;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import redis.clients.jedis.Jedis;

import java.io.IOException;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;
import static io.vertx.example.web.proxy.filter.Filter.FilterBuilder.filterBuilder;

@RunWith(VertxUnitRunner.class)
public class ProxyToRestRedisTest {

    public static final int PROXY_PORT = 8080;
    public static final int REST_PORT = 8082;
    public static final String LOCALHOST = "localhost";
//SERVICES
    public static final String SERVICE_A_OPEN = "serviceA";
    public static final String SERVICE_B_BLOCKED = "serviceB";
//PRODUCTS
    public static final String PROD3568_BLOCKED = "prod3568";
    public static final String PROD7340_OPED = "prod7340";
    public static final String PROD8643_OPEN = "prod8643";
    public static final String PROXY = "PROXY";

    private static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException, InterruptedException {
        //start verticals
        vertx = Vertx.vertx();
        //start redis client
        Jedis client = new Jedis("localhost");

        //deploy redis server
        vertx.deployVerticle(new RedisStarted(client), context.asyncAssertSuccess());
        //deploy rest server
        vertx.deployVerticle(new SimpleREST(new RedisReporter(client,25 )),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST_PORT)),
                context.asyncAssertSuccess());

        //keys & routes keysRepository
        KeysRepository keysRepository = new RedisKeysRepository(client);

        //proxy vertical deployment
        vertx.deployVerticle(new ProxyServer(
                        filterBuilder(keysRepository)
                                .add(new ServiceFilter())
                                .add(new ProductFilter())
                                .build(),
                        new RedisReporter(client,25 ),
                        new RedisServiceLocator(client, SimpleREST.REST)),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put(HTTP_PORT, PROXY_PORT)
                        .put(ENABLE_METRICS_PUBLISH, false)),
                context.asyncAssertSuccess());
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
        String requestURI = "/" + SERVICE_A_OPEN;
        client.getNow(PROXY_PORT, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(PROD3568_BLOCKED));
                context.assertTrue(body.toString().contains(PROD8643_OPEN));
                context.assertTrue(body.toString().contains(PROD8643_OPEN));
                async.complete();
                client.close();
            });
        });
    }

    @Test
    public void testServiceAProd3568(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String requestURI = "/" + SERVICE_A_OPEN + "/" + PROD3568_BLOCKED;
        client.getNow(PROXY_PORT, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
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
        String requestURI = "/" + SERVICE_B_BLOCKED;
        client.getNow(PROXY_PORT, LOCALHOST , requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
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
        String requestURI = "/" + SERVICE_A_OPEN + "/" + PROD7340_OPED;
        client.getNow(PROXY_PORT, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(PROD7340_OPED));
                client.close();
                async.complete();
            });
        });
    }
}
