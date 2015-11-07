package io.vertx.example.web.proxy.locator;

import io.vertx.example.web.proxy.filter.FilterUtils;

import java.util.Optional;
import java.util.Set;

public class InMemServiceLocator implements ServiceLocator {
    private String domain;
    private RoundRobinPool pool;

    public InMemServiceLocator(String domain, Set<String> services) {
        this.domain = domain;
        this.pool = new RoundRobinPool();
        //update keys in pool - if absent
        pool.addServices(domain, services);
    }

    @Override
    public Optional<String> getService(String uri) {
        Optional<String> service = FilterUtils.extractService(uri);
        if (!service.isPresent()) {
            return Optional.empty();
        }
        //get next (circular loop) round robin
        return pool.get(service.get());
    }

    public String getDomain() {
        return domain;
    }
}

