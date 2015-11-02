package io.vertx.example.web.proxy.repository;

import io.vertx.example.web.proxy.filter.ParseUtils;

import java.util.Map;
import java.util.Optional;

import static io.vertx.example.web.proxy.filter.ParseUtils.*;

/**
 * Created by INTERNET on 29/10/2015.
 */
public interface Repository {

    public Map<String, String> getServices();

    public Map<String, String> getProducts();

    public Optional<Boolean> getService(String uri);

    public Optional<Boolean> getProduct(String uri);


}
