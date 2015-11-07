package io.vertx.example.web.proxy.locator;

import io.vertx.example.web.proxy.filter.FilterUtils;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.Set;

public class RedisServiceLocator implements ServiceLocator{
    private Jedis client;
    private String domain;
    private RoundRobinPool pool;

    public RedisServiceLocator(Jedis client, String domain) {
        this.client = client;
        this.domain = domain;
        this.pool = new RoundRobinPool();
    }

    @Override
    public Optional<String> getService(String uri) {
        Optional<String> service = FilterUtils.extractService(uri);
        if(!service.isPresent()) {
            return Optional.empty();
        }
        //update keys in pool - if absent
        Set<String> keys = client.keys(domain + "." + service + "*");
        pool.addServices(service.get(), keys);
        //get next (circular loop) round robin
        return pool.get(service.get());
    }

    @Override
    public String getDomain() {
        return domain;
    }
}
