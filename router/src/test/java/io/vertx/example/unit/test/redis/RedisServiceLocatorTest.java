package io.vertx.example.unit.test.redis;

import io.vertx.core.Vertx;
import io.vertx.example.unit.test.integration.RedisStarted;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.example.web.proxy.healthcheck.RedisHealthReporter;
import io.vertx.example.web.proxy.healthcheck.HealthReporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.RedisServiceLocator;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.Inet4Address;
import java.util.Optional;

import static io.vertx.example.unit.test.integration.RedisStarted.populate;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class RedisServiceLocatorTest {
    //SERVICES
    public static final String SERVICE_A_OPEN = "serviceA";
    public static final String SERVICE_B_BLOCKED = "serviceB";
    //PRODUCTS
    public static final String PROD3568_BLOCKED = "prod3568";
    public static final String PROD7340_OPED = "prod7340";
    public static final String PROD8643_OPEN = "prod8643";
    public static final int PORT = 8080;
    public static final int PORT1 = 8081;
    public static final String REST = "REST";
    public static final String VERSION = "1.0";

    private static Vertx vertx;
    private static RedisHealthReporter reporter;
    private static VerticalServiceRegistry verticalServiceRegistry;
    private static long timer;
    private static JedisPool pool;


    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        Async async = context.async();
        vertx = Vertx.vertx(VertxInitUtils.initOptions());
        pool = RedisStarted.getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        populate(jedis);
        jedis.close();

        //deploy embedded redis
//        vertx.deployVerticle(new RedisStarted(jedis),context.asyncAssertSuccess());

        reporter = new RedisHealthReporter(pool, 1000);
        verticalServiceRegistry = new VerticalServiceRegistry();
        verticalServiceRegistry.register(ServiceDescriptor.create(SERVICE_A_OPEN, PORT));
        verticalServiceRegistry.register(ServiceDescriptor.create(SERVICE_B_BLOCKED, PORT));
        //set services health checks
        timer = HealthReporter.setUpHealthCheck(vertx, REST, verticalServiceRegistry, reporter, 2000);
        async.complete();
    }

    @Test
    public void testRepositoryGetServices() throws Exception {
        //service locator
        RedisServiceLocator locator = new RedisServiceLocator(pool, REST);
        assertEquals(locator.getDomain(), REST);

        Optional<ServiceDescriptor> locatorService = locator.getService("/" + SERVICE_A_OPEN + "/" + PROD7340_OPED, VERSION);
        assertTrue(locatorService.isPresent());
        String hostAddress = Inet4Address.getLocalHost().getHostAddress();
        assertEquals(locatorService.get().getHost(), hostAddress );
        assertEquals(locatorService.get().getPort(), PORT );

        locatorService = locator.getService("/" + SERVICE_B_BLOCKED + "/" + PROD8643_OPEN, VERSION);
        assertTrue(locatorService.isPresent());
        assertEquals(locatorService.get().getHost(), hostAddress );
        assertEquals(locatorService.get().getPort(), PORT );

    }

    @AfterClass
    public static void tearDown(TestContext context) {
        pool = RedisStarted.getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        jedis.close();
        vertx.cancelTimer(timer);
        vertx.close(context.asyncAssertSuccess());
    }


}
