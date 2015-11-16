package io.vertx.example.unit.test.redis;

import io.vertx.core.Vertx;
import io.vertx.example.web.proxy.RedisStarted;
import io.vertx.example.web.proxy.healthcheck.RedisReporter;
import io.vertx.example.web.proxy.healthcheck.Reporter;
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
import org.junit.runners.BlockJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.Inet4Address;
import java.util.Optional;

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

    private static Jedis client;
    private static Vertx vertx;
    private static RedisReporter reporter;
    private static VerticalServiceRegistry verticalServiceRegistry;

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        Async async = context.async();
        vertx = Vertx.vertx();
        JedisPool pool = RedisStarted.getJedisPool("localhost");
        client = pool.getResource();
//        vertx.deployVerticle(new RedisStarted(client),context.asyncAssertSuccess());
        reporter = new RedisReporter(client, 1000);
        verticalServiceRegistry = new VerticalServiceRegistry();
        async.complete();
    }

    @Test
    public void testRepositoryGetServices() throws Exception {
        verticalServiceRegistry.register(ServiceDescriptor.create(SERVICE_A_OPEN, PORT));
        verticalServiceRegistry.register(ServiceDescriptor.create(SERVICE_B_BLOCKED, PORT));
        //set services health checks
        Reporter.setUpHealthCheck(vertx,REST, verticalServiceRegistry,reporter);
        //service locator
        RedisServiceLocator locator = new RedisServiceLocator(client, REST);
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
        client.close();
        vertx.close(context.asyncAssertSuccess());
    }


}
