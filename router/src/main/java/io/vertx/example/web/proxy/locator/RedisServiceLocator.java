package io.vertx.example.web.proxy.locator;

import com.google.common.collect.Sets;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.example.web.proxy.filter.FilterUtils;
import redis.clients.jedis.Jedis;

import java.util.Collection;
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
        Set<String> keys = client.keys(domain + "." + service.get() + "*");
        //get values according to keys from redis
        String[] values = keys.stream().map(s -> client.get(s)).toArray(String[]::new );
        pool.addServices(service.get(),Sets.newHashSet(values));
        //get next (circular loop) round robin
        return pool.get(service.get());
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        if(client.isConnected()) {
            client.close();
        }
    }

    @Override
    public Collection<String> getAllProviders(String serviceName) {
        return client.keys(domain + "." + serviceName + "*");
    }

}
