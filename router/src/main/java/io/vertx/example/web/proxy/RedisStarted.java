package io.vertx.example.web.proxy;

import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * Created by lior on 28/10/2015.
 */
public class RedisStarted  {
    public static final String SERVICES = "services";
    public static final String PRODUCTS = "products";
    private final Jedis jedis;

    public RedisStarted() {
        jedis = new Jedis("localhost");
    }

    public static void main(String[] args) throws Exception {
        new RedisStarted().start();
    }

    public void start() throws Exception {
        launchRedis();
    }

    private void launchRedis() throws IOException {
        RedisServer redisServer = new RedisServer(6379);
        redisServer.start();
        populate();
        System.out.println("Starting redis:6379");

    }

    private void populate() {
        System.out.println(" ************** populate redis **************");
        System.out.println(" Create services set");
        System.out.println(" >> hset services serviceA true");
        jedis.hset(SERVICES, "serviceA", "true");
        System.out.println(" >> hset services serviceB false");
        jedis.hset(SERVICES, "serviceB", "false");

        System.out.println(" Create products set");
        System.out.println(" >> hset products prod3568 false");
        jedis.hset(PRODUCTS, "prod3568", "false");

    }

}
