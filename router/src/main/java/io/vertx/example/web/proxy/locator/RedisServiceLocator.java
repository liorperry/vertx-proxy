package io.vertx.example.web.proxy.locator;

import com.google.common.collect.Sets;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.filter.FilterUtils;
import io.vertx.example.web.proxy.healthcheck.Reporter;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
        String query = "\\\""+ domain + "\\." + serviceVersion.getKey() + "*";
        System.out.println("Fetching keys for provider[" + serviceVersion.getKey() + "] query:" + query);
        System.out.println("client connected:" + client.isConnected());
        Set<String> keys = client.keys(query);
        if(keys==null || keys.isEmpty())
            return Collections.emptySet();
        //get values according to keys from redis
        Stream<String> stringStream = keys.stream().map(client::get);
        return Sets.newHashSet(stringStream.map(s -> ServiceDescriptor.create(new JsonObject(s))).toArray(ServiceDescriptor[]::new));

    }

}
