package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.Repository;

import java.util.Optional;

import static io.vertx.example.web.proxy.filter.FilterUtils.extractProduct;


public class ProductFilter implements FilterPhase {
    @Override
    public boolean filter(HttpServerRequest request, Repository repository) {
        String uri = request.uri();
        Optional<Boolean> product = repository.getProduct(uri);
        System.out.println("filter uri :" + uri +" product "+ extractProduct(uri) + "["+product+"]");
        if(product.isPresent())
            return product.get();
        //default value for non registered products
        return true;
    }
}
