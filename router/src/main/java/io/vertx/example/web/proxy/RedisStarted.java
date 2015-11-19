package io.vertx.example.web.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.embedded.RedisServer;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.IOException;

public class RedisStarted extends AbstractVerticle {
    public static final String SERVICES = "services";
    public static final String PRODUCTS = "products";
    public static final String CHANNEL_INTERNET = "channel.internet";
    private static RedisServer redisServer;

    private final Jedis jedis;

    public RedisStarted(Jedis client) {
        jedis = client;
    }

    public static void main(String[] args) throws Exception {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RedisStarted(new Jedis()));
    }

    public void start(Future<Void> fut) {
        try {
            launchRedis();
            fut.complete();
        } catch (IOException e) {
            fut.fail(e);
        }
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
        try {
            redisServer.stop();
            stopFuture.complete();
        } catch (EmbeddedRedisException e) {
            stopFuture.fail(e);
        }
    }

    private void launchRedis() throws IOException {
        startRedis();
        populate(jedis);

    }

    public static void startRedis() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();
        System.out.println("Starting redis:6379");
    }

    public static void populate(Jedis jedis) {
        try {
            System.out.println(" ************** populate redis **************");
            System.out.println(" Create services set");
            System.out.println(" >> hset services serviceA true");
            jedis.hset(SERVICES, "serviceA", "true");
            System.out.println(" >> hset services serviceB false");
            jedis.hset(SERVICES, "serviceB", "false");
            System.out.println(" >> hset services whoAmI true");
            jedis.hset(SERVICES, "whoAmI", "true");

            System.out.println(" Create products set");
            System.out.println(" >> hset products prod3568 false");
            jedis.hset(PRODUCTS, "prod3568", "false");

            System.out.println(" Create channel services set");
            System.out.println(" >> sadd channel.internet serviceA serviceB");
            jedis.sadd(CHANNEL_INTERNET, "serviceA", "serviceB", "whoAmI");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JedisPool getJedisPool(String host) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMaxWaitMillis(1000);
        return new JedisPool(jedisPoolConfig,host);

    }
}
