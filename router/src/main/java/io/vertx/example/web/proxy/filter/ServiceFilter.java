package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.Repository;

import static io.vertx.example.web.proxy.repository.Repository.extractService;

public class ServiceFilter implements FilterPhase {
    @Override
    public boolean filter(HttpServerRequest request, Repository repository) {
        String uri = request.uri();
        boolean result = repository.getService(uri);
        System.out.println("filter uri :" + uri +" service "+ extractService(uri) + "["+result+"]");
        return result;
    }
}
