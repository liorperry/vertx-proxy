package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.KeysRepository;

import java.util.Optional;

import static io.vertx.example.web.proxy.filter.FilterUtils.*;

public class ServiceFilter implements FilterPhase {
    @Override
    public boolean filter(HttpServerRequest request, KeysRepository keysRepository) {
        String uri = request.uri();
        Optional<Boolean> service = keysRepository.getService(uri);
        System.out.println("filter uri :" + uri +" service "+ extractService(uri) + "["+service+"]");
        if(service.isPresent())
            return service.get();
        //default response for non registered services
        return false;
    }
}
