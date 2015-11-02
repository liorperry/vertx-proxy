package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.Repository;

import static io.vertx.example.web.proxy.repository.Repository.extractProduct;
import static io.vertx.example.web.proxy.repository.Repository.extractService;

public class ProductFilter implements FilterPhase {
    @Override
    public boolean filter(HttpServerRequest request, Repository repository) {
        String uri = request.uri();
        boolean result = repository.getProduct(uri);
        System.out.println("filter uri :" + uri +" product "+ extractProduct(uri) + "["+result+"]");
        return result;
    }
}
