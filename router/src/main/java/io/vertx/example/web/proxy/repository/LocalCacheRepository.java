package io.vertx.example.web.proxy.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.vertx.example.web.proxy.filter.FilterUtils.extractProduct;
import static io.vertx.example.web.proxy.filter.FilterUtils.extractService;

public class LocalCacheRepository implements Repository {

    private ConcurrentHashMap<String,String> servicesMap;
    private ConcurrentHashMap<String,String> productsMap;

    public LocalCacheRepository() {
        servicesMap = new ConcurrentHashMap<>();
        productsMap = new ConcurrentHashMap<>();
    }

    @Override
    public Map<String, String> getServices() {
        return servicesMap;
    }

    @Override
    public Map<String, String> getProducts() {
        return productsMap;
    }

    @Override
    public Optional<Boolean> getService(String uri) {
        Optional<String> service = extractService(uri);
        if(!service.isPresent()) {
            return Optional.empty();
        }

        String value = servicesMap.get(service.get());
        if(value==null) {
            System.out.println(" Service "+service +" not found in keys set");
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(value));
    }

    @Override
    public Optional<Boolean> getProduct(String uri) {
        Optional<String> product = extractProduct(uri);
        if(!product.isPresent()) {
            return Optional.empty();
        }
        String value = productsMap.get(product.get());
        if(value==null) {
            System.out.println(" Product "+product +" not found in keys set");
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(value));
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {}
}
