package io.vertx.example.web.proxy.locator;

import com.google.common.collect.Sets;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
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
    public Optional<ServiceDescriptor> getService(String uri, String version) {
        Optional<String> service = FilterUtils.extractService(uri);
        if(!service.isPresent()) {
            return Optional.empty();
        }
        pool.addServices(Sets.newHashSet(getAllProviders(new ServiceVersion(service.get(),version))));
        //get next (circular loop) round robin
        return pool.get(new ServiceVersion(service.get(),version));
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
    public Collection<ServiceDescriptor> getAllProviders(ServiceVersion serviceVersion) {
        //update keys in pool - if absent
        Set<String> keys = client.keys(domain + "." + serviceVersion.getName() + "*");
        //get values according to keys from redis
        return Sets.newHashSet(keys.stream().map(s -> ServiceDescriptor.create(new JsonObject(client.get(s)))).toArray(ServiceDescriptor[]::new));
    }

}
