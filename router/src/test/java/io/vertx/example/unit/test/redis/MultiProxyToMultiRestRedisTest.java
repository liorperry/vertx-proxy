package io.vertx.example.unit.test.redis;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.web.proxy.RedisStarted;
import io.vertx.example.web.proxy.SimpleREST;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.healthcheck.RedisHealthReporter;
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
import redis.clients.jedis.JedisPool;

import java.io.IOException;

import static io.vertx.example.web.proxy.RedisStarted.getJedisPool;
import static io.vertx.example.web.proxy.RedisStarted.populate;
import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;
import static io.vertx.example.web.proxy.filter.Filter.FilterBuilder.filterBuilder;

@RunWith(VertxUnitRunner.class)
public class MultiProxyToMultiRestRedisTest {

    public static final int PROXY_PORT = 8080;
    public static final int REST1_PORT = 8082;
    public static final int REST2_PORT = 8083;
    public static final String LOCALHOST = "localhost";
    //SERVICES
    public static final String WHO_AM_I = "whoAmI";

    private static Vertx vertx;
    private static KeysRepository keysRepository;
    private static Jedis client;
    private static JedisPool pool;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException, InterruptedException {
        //start verticals
        vertx = Vertx.vertx();
        //start redis client
        pool = getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        populate(jedis);
        jedis.close();

        //deploy redis server
//        vertx.deployVerticle(new RedisStarted(client), context.asyncAssertSuccess());
        //deploy rest server
        vertx.deployVerticle(new SimpleREST(new RedisHealthReporter(pool, 25)),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST1_PORT)),
                context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleREST(new RedisHealthReporter(pool, 25)),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST2_PORT)),
                context.asyncAssertSuccess());

        //keys & routes keysRepository
        keysRepository = new RedisKeysRepository(pool);

        //proxy vertical deployment
        vertx.deployVerticle(new ProxyServer(
                        filterBuilder(keysRepository)
                                .add(new ServiceFilter())
                                .add(new ProductFilter())
                                .build(),
                        new RedisHealthReporter(pool, 25),
                        new RedisServiceLocator(pool, SimpleREST.REST)),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put(HTTP_PORT, PROXY_PORT)
                        .put(ENABLE_METRICS_PUBLISH, false)),
                context.asyncAssertSuccess());
    }

    @Test
    public void testServiceWhoAmIFirst(TestContext context) throws InterruptedException {
        Async async = context.async();
        // Send a request and get a response
        final String requestURI = "/" + WHO_AM_I;
        HttpClient httpClient = vertx.createHttpClient();

        //first request
        httpClient.getNow(PROXY_PORT, LOCALHOST, requestURI, resp1 -> {
            resp1.bodyHandler(body1 -> {
                JsonObject entries1 = new JsonObject(body1.toString());
                System.out.println(requestURI + ":" + entries1.encodePrettily());
                async.complete();
            });
        });

    }

    @Test
    public void testServiceWhoAmISecond(TestContext context) throws InterruptedException {
        Async async = context.async();
        // Send a request and get a response
        final String requestURI = "/" + WHO_AM_I;
        HttpClient httpClient = vertx.createHttpClient();

        //first request
        httpClient.getNow(PROXY_PORT, LOCALHOST, requestURI, resp1 -> {
            resp1.bodyHandler(body1 -> {
                JsonObject entries1 = new JsonObject(body1.toString());
                System.out.println(requestURI + ":" + entries1.encodePrettily());
                async.complete();
            });
        });
    }

    @AfterClass
    public static void tearDown() throws Exception {
        pool = RedisStarted.getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        jedis.close();
        vertx.close();
    }
}
