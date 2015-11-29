package io.vertx.example.web.proxy.locator;

import com.google.common.collect.Sets;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.filter.FilterUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CouchbaseServiceLocator implements ServiceLocator {
    private JedisPool redisPool;
    private String domain;
    private RoundRobinPool pool;

    public CouchbaseServiceLocator(JedisPool redisPool, String domain) {
        this.redisPool = redisPool;
        this.domain = domain;
        this.pool = new RoundRobinPool();
    }

    @Override
    public Optional<ServiceDescriptor> getService(String uri, String version) {
        Optional<String> service = FilterUtils.extractService(uri);
        if (!service.isPresent()) {
            return Optional.empty();
        }
        ServiceVersion serviceVersion = new ServiceVersion(service.get(), version);
        Collection<ServiceDescriptor> providers = getAllProviders(serviceVersion);
        if (providers.size() != pool.getAll(serviceVersion).size()) {
            pool.addServices(Sets.newHashSet(providers));
        }
        //get next (circular loop) round robin
        return pool.get(serviceVersion);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        redisPool.close();
    }

    @Override
    public Collection<ServiceDescriptor> getAllProviders(ServiceVersion serviceVersion) {
        Jedis client = redisPool.getResource();
        try {
            //update keys in pool - if absent
            String query = "\\\"" + domain + "\\." + serviceVersion.getKey() + "*";
            System.out.println("Fetching keys for provider[" + serviceVersion.getKey() + "] query:" + query);
            System.out.println("client connected:" + client.isConnected());
            Set<String> keys = client.keys(query);
            if (keys == null || keys.isEmpty())
                return Collections.emptySet();
            //get values according to keys from redis
            Stream<String> stringStream = keys.stream().map(client::get);
            return Sets.newHashSet(stringStream.map(s -> ServiceDescriptor.create(new JsonObject(s))).toArray(ServiceDescriptor[]::new));
        } finally {
            client.close();
        }
    }

    @Override

    public Collection<ServiceDescriptor> getAllProviders() {
        Jedis client = redisPool.getResource();
        try {
            //update keys in pool - if absent
            String query = "\\\"" + domain + "\\." + "*";
            System.out.println("Fetching keys for all providers query:" + query);
            System.out.println("client connected:" + client.isConnected());
            Set<String> keys = client.keys(query);
            if (keys == null || keys.isEmpty())
                return Collections.emptySet();
            //get values according to keys from redis
            Stream<String> stringStream = keys.stream().map(client::get);
            return Sets.newHashSet(stringStream.map(s -> ServiceDescriptor.create(new JsonObject(s))).toArray(ServiceDescriptor[]::new));
        } finally {
            client.close();
        }
    }


    @Override
    public void updateFromRegistry() {
        pool.addServices(Sets.newHashSet(getAllProviders()));
    }

}
