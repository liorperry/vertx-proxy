package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.KeysRepository;

import java.util.Optional;

import static io.vertx.example.web.proxy.filter.FilterUtils.extractProduct;


public class ProductFilter implements FilterPhase {
    @Override
    public boolean filter(HttpServerRequest request, KeysRepository keysRepository) {
        String uri = request.uri();
        Optional<Boolean> product = keysRepository.getProduct(uri);
        System.out.println("filter uri :" + uri +" product "+ extractProduct(uri) + "["+product+"]");
        if(product.isPresent())
            return product.get();
        //default value for non registered products
        return true;
    }
}
