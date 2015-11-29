package io.vertx.example.unit.test.redis;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.unit.test.integration.RedisStarted;
import io.vertx.example.unit.test.integration.SimpleREST;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.healthcheck.RedisHealthReporter;
import io.vertx.example.web.proxy.locator.RedisServiceLocator;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.example.web.proxy.repository.KeysRepository;
import io.vertx.example.web.proxy.repository.RedisKeysRepository;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

import static io.vertx.example.unit.test.integration.RedisStarted.populate;
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
    private static Jedis client;
    private static JedisPool pool;

    @Before
    public  void setUp(TestContext context) throws IOException, InterruptedException {
        Async async = context.async();
        //start verticals
        vertx = Vertx.vertx(VertxInitUtils.initOptions());
        //start redis client
        pool = RedisStarted.getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        populate(jedis);
        jedis.close();

        //deploy redis server
//        vertx.deployVerticle(new RedisStarted(client), context.asyncAssertSuccess());
        //deploy rest server
        vertx.deployVerticle(new SimpleREST(new RedisHealthReporter(pool,100 ),Publisher.EMPTY, Publisher.EMPTY, new VerticalServiceRegistry()),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST_PORT)),
                context.asyncAssertSuccess());

        //keys & routes keysRepository
        KeysRepository keysRepository = new RedisKeysRepository(pool);

        //proxy vertical deployment
        vertx.deployVerticle(new ProxyServer(
                        filterBuilder(keysRepository)
                                .add(new ServiceFilter())
                                .add(new ProductFilter())
                                .build(),
                        new RedisHealthReporter(pool,100 ),
                        Publisher.EMPTY,
                        new VerticalServiceRegistry(),
                        new RedisServiceLocator(pool, SimpleREST.REST)),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put(HTTP_PORT, PROXY_PORT)
                        .put(ENABLE_METRICS_PUBLISH, false)),
                context.asyncAssertSuccess());
        //setup completed
        async.complete();
    }

    @After
    public  void tearDown(TestContext context) {
        pool = RedisStarted.getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        jedis.close();
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
