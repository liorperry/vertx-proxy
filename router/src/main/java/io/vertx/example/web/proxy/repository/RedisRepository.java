package io.vertx.example.web.proxy.repository;

import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Map;

import static io.vertx.example.web.proxy.repository.Repository.*;

/**
 * Created by INTERNET on 29/10/2015.
 */
public class RedisRepository implements Repository {
    public static final String SERVICES = "services";
    public static final String PRODUCTS = "products";

    private Jedis jedis;

    public RedisRepository() {
        // Create the redis client
        jedis = new Jedis("localhost");
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
    public boolean getService(String uri) {
        String service = extractService(uri);
        String value = jedis.hget(SERVICES, service);
        if(value==null) {
            System.out.println(" Service "+service +" not found in keys set");
        }
        return Boolean.valueOf(value);
    }

    @Override
    public boolean getProduct(String uri) {
        String product = extractProduct(uri);
        String value = jedis.hget(PRODUCTS, product);
        if(value==null) {
            System.out.println(" Product "+product +" not found in keys set");
        }
        return Boolean.valueOf(value);
    }

}
