package io.vertx.example.web.proxy.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.vertx.example.web.proxy.filter.FilterUtils.extractProduct;
import static io.vertx.example.web.proxy.filter.FilterUtils.extractService;
import static java.lang.Boolean.TRUE;

public class LocalCacheKeysRepository implements KeysRepository {

    private ConcurrentHashMap<String, String> servicesMap;
    private ConcurrentHashMap<String, String> productsMap;
    private ConcurrentHashMap<String, Set<String>> channelServicesMap;

    public LocalCacheKeysRepository() {
        servicesMap = new ConcurrentHashMap<>();
        productsMap = new ConcurrentHashMap<>();
        channelServicesMap = new ConcurrentHashMap<>();
    }

    public LocalCacheKeysRepository(Map<String, String> servicesMap) {
        this.servicesMap = new ConcurrentHashMap<>(servicesMap);
        productsMap = new ConcurrentHashMap<>();
    }

    public LocalCacheKeysRepository(Map<String, String> servicesMap, Map<String, String> productsMap) {
        this.servicesMap = new ConcurrentHashMap<>(servicesMap);
        this.productsMap = new ConcurrentHashMap<>(productsMap);
    }


    @Override
    public Map<String, String> getServices() {
        return Collections.unmodifiableMap(servicesMap);
    }

    @Override
    public Map<String, String> getProducts() {
        return Collections.unmodifiableMap(productsMap);
    }

    @Override
    public Set<String> getChannelServices(String channelName) {
        return Collections.emptySet();
    }

    @Override
    public Optional<Boolean> getService(String uri) {
        Optional<String> service = extractService(uri);
        if (!service.isPresent()) {
            return Optional.empty();
        }

        String value = servicesMap.get(service.get());
        if (value == null) {
            System.out.println(" Service " + service + " not found in keys set");
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(value));
    }

    @Override
    public Optional<Boolean> getChannelService(String uri, String channelName) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getProduct(String uri) {
        Optional<String> product = extractProduct(uri);
        if (!product.isPresent()) {
            return Optional.empty();
        }
        String value = productsMap.get(product.get());
        if (value == null) {
            System.out.println(" Product " + product + " not found in keys set");
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(value));
    }

    @Override
    public boolean blockProduct(String productName) {
        return Boolean.parseBoolean(productsMap.put(productName, Boolean.FALSE.toString()));
    }

    @Override
    public boolean openProduct(String productName) {
        return Boolean.parseBoolean(productsMap.put(productName, TRUE.toString()));
    }

    @Override
    public void addService(String serviceName, boolean status, String ... channels) {
        servicesMap.put(serviceName, Boolean.toString(status));
        if(!channelServicesMap.containsKey(serviceName)) {
            channelServicesMap.put(serviceName,new HashSet<>());
        }
        Collections.addAll(channelServicesMap.get(serviceName), channels);
    }

    @Override
    public void addProduct(String productName, boolean status) {
        productsMap.put(productName, Boolean.toString(status));
    }

    @Override
    public boolean blockService(String serviceName) {
        return Boolean.parseBoolean(servicesMap.put(serviceName, Boolean.FALSE.toString()));
    }

    @Override
    public boolean openService(String serviceName) {
        return Boolean.parseBoolean(servicesMap.put(serviceName, TRUE.toString()));
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        servicesMap.clear();
        productsMap.clear();
    }

    public static LocalCacheKeysRepository create(Map<String, String> map) {
        return new LocalCacheKeysRepository(map);
    }
}
