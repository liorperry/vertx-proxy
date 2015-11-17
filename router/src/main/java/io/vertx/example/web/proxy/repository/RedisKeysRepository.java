package io.vertx.example.web.proxy.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.vertx.example.web.proxy.filter.FilterUtils.*;

public class RedisKeysRepository implements KeysRepository {
    public static final String SERVICES = "services";
    public static final String PRODUCTS = "products";

    private JedisPool pool;

    public RedisKeysRepository(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public Map<String, String> getServices() {
        Jedis jedis = pool.getResource();
        Map<String, String> map = jedis.hgetAll(SERVICES);
        jedis.close();
        if(map!=null)
            return map;
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getProducts() {
        Jedis jedis = pool.getResource();
        Map<String, String> map = jedis.hgetAll(PRODUCTS);
        jedis.close();
        if(map!=null)
            return map;
        return Collections.emptyMap();
    }

    @Override
    public Optional<Boolean> getService(String uri) {
        Optional<String> service = extractService(uri);
        if(!service.isPresent()) {
            return Optional.empty();
        }

//        String value = jedis.hget(SERVICES, service.get());
        Jedis jedis = pool.getResource();
        Map<String, String> map = jedis.hgetAll(SERVICES);
        jedis.close();
        if(map==null || !map.containsKey(service.get())) {
            System.out.println(" Service "+service +" not found in keys set");
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(map.get(service.get())));
    }

    @Override
    public Optional<Boolean> getProduct(String uri) {
        Optional<String> product = extractProduct(uri);
        if(!product.isPresent()) {
            return Optional.empty();
        }
        Jedis jedis = pool.getResource();
        String value = null;
        try {
            value = jedis.hget(PRODUCTS, product.get());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        if(value==null) {
            System.out.println(" Product "+product +" not found in keys set");
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(value));
    }

    @Override
    public boolean blockService(String serviceName) {
        Jedis jedis = pool.getResource();
        jedis.hset(SERVICES,serviceName,Boolean.FALSE.toString());
        jedis.close();
        return true;
    }

    @Override
    public boolean openService(String serviceName) {
        Jedis jedis = pool.getResource();
        jedis.hset(SERVICES,serviceName,Boolean.TRUE.toString());
        jedis.close();
        return true;
    }

    @Override
    public boolean blockProduct(String productName) {
        Jedis jedis = pool.getResource();
        jedis.hset(PRODUCTS, productName, Boolean.FALSE.toString());
        jedis.close();
        return false;
    }

    @Override
    public boolean openProduct(String productName) {
        Jedis jedis = pool.getResource();
        jedis.hset(PRODUCTS, productName, Boolean.TRUE.toString());
        jedis.close();
        return false;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        pool.close();
    }

    @Override
    public void addService(String serviceName, boolean status) {
        Jedis jedis = pool.getResource();
        jedis.hset(SERVICES, serviceName, Boolean.toString(status));
        jedis.close();
    }

    @Override
    public void addProduct(String productName, boolean status) {
        Jedis jedis = pool.getResource();
        jedis.hset(PRODUCTS, productName, Boolean.toString(status));
        jedis.close();
    }
}
