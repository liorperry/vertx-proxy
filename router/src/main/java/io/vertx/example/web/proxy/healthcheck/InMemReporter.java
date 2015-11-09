package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.repository.LocalCacheRepository;
import io.vertx.example.web.proxy.repository.Repository;

public class InMemReporter implements Reporter{

    private LocalCacheRepository repository;

    public InMemReporter(LocalCacheRepository repository) {
        this.repository = repository;
    }

    @Override
    public HealthCheck.Result report(HealthCheck.Result result, String domain, ServiceDescriptor descriptor) {
        repository.a
        return null;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {

    }
}
