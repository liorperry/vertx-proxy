package io.vertx.example.web.proxy.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.vertx.example.web.proxy.filter.FilterUtils.*;

public class RedisRepository implements Repository {
    public static final String SERVICES = "services";
    public static final String PRODUCTS = "products";

    private Jedis jedis;

    public RedisRepository(Jedis client) {
        // Create the redis client
        jedis = client;
    }

    @Override
    public Map<String, String> getServices() {
        Map<String, String> map = jedis.hgetAll(SERVICES);
        if(map!=null)
            return map;
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getProducts() {
        Map<String, String> map = jedis.hgetAll(PRODUCTS);
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
        Map<String, String> map = jedis.hgetAll(SERVICES);
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
        String value = jedis.hget(PRODUCTS, product.get());
        if(value==null) {
            System.out.println(" Product "+product +" not found in keys set");
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(value));
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        if(jedis.isConnected()) {
            jedis.close();
        }
    }

}
