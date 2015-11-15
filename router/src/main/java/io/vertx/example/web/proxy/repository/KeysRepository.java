package io.vertx.example.web.proxy.repository;

import io.vertx.core.impl.Closeable;
import io.vertx.example.web.proxy.filter.ParseUtils;

import java.util.Map;
import java.util.Optional;

import static io.vertx.example.web.proxy.filter.ParseUtils.*;

public interface KeysRepository extends Closeable {

    Map<String, String> getServices();

    Map<String, String> getProducts();

    Optional<Boolean> getService(String uri);

    Optional<Boolean> getProduct(String uri);

    boolean blockService(String serviceName);

    boolean openService(String serviceName);

    boolean blockProduct(String productName);

    boolean openProduct(String productName);

    void addService(String serviceName, boolean status);

    void addProduct(String productName, boolean status);
}
