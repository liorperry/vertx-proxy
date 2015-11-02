package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.Repository;

/**
 * single phase filter - a part of chain of filters
 */
public interface FilterPhase {

    public boolean filter(HttpServerRequest request, Repository repository);

}
