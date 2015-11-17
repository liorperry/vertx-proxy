package io.vertx.example.unit.test.redis;

import io.vertx.core.Vertx;
import io.vertx.example.web.proxy.RedisStarted;
import io.vertx.example.web.proxy.repository.RedisKeysRepository;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.Optional;

import static io.vertx.example.web.proxy.RedisStarted.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class RedisKeysRepositoryTest {
    //SERVICES
    public static final String SERVICE_A_OPEN = "serviceA";
    public static final String SERVICE_B_BLOCKED = "serviceB";
    public static final String WHO_AM_I = "whoAmI";
    //PRODUCTS
    public static final String PROD3568_BLOCKED = "prod3568";
    public static final String PROD7340_OPED = "prod7340";
    public static final String PROD8643_OPEN = "prod8643";

    private static Jedis client;
    private static Vertx vertx;
    private static JedisPool pool;

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        vertx = Vertx.vertx();
        pool = RedisStarted.getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        populate(jedis);
        jedis.close();


//        vertx.deployVerticle(new RedisStarted(client),context.asyncAssertSuccess());
    }

    @Test
    public void testRepositoryGetServices() throws Exception {
        RedisKeysRepository repository = new RedisKeysRepository(pool);
        Map<String, String> services = repository.getServices();
        assertEquals(services.size(),3);
        assertTrue(Boolean.valueOf(services.get(SERVICE_A_OPEN)));
        assertTrue(Boolean.valueOf(services.get(WHO_AM_I)));
        assertTrue(!Boolean.valueOf(services.get(SERVICE_B_BLOCKED)));

        Optional<Boolean> service = repository.getService("/" + SERVICE_A_OPEN + "/" + PROD7340_OPED);
        assertTrue(service.isPresent());
        assertTrue(service.get().booleanValue());
    }

    @Test
    public void testRepositoryGetProducts() throws Exception {
        RedisKeysRepository repository = new RedisKeysRepository(pool);
        Map<String, String> products = repository.getProducts();
        assertEquals(products.size(),1);
        assertTrue(!Boolean.valueOf(products.get(PROD3568_BLOCKED)));

        Optional<Boolean> product = repository.getProduct("/" + SERVICE_A_OPEN + "/" + PROD3568_BLOCKED);
        assertTrue(product.isPresent());
        assertTrue(!product.get().booleanValue());

    }

    @AfterClass
    public static void tearDown(TestContext context) {
        pool = RedisStarted.getJedisPool("localhost");
        Jedis jedis = pool.getResource();
        jedis.flushDB();
        jedis.close();
        vertx.close(context.asyncAssertSuccess());
    }


}
