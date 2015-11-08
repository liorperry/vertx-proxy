package io.vertx.example.web.proxy.repository;

import io.vertx.core.impl.Closeable;
import io.vertx.example.web.proxy.filter.ParseUtils;

import java.util.Map;
import java.util.Optional;

import static io.vertx.example.web.proxy.filter.ParseUtils.*;

public interface Repository extends Closeable {

    public Map<String, String> getServices();

    public Map<String, String> getProducts();

    public Optional<Boolean> getService(String uri);

    public Optional<Boolean> getProduct(String uri);


}
