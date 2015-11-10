package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.netty.util.internal.ConcurrentSet;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;

import java.util.Set;

public class InMemReporter implements Reporter{

    private Set<String> services;

    public InMemReporter(Set<String> services) {
        this.services = services;
    }

    @Override
    public HealthCheck.Result report(HealthCheck.Result result, String domain, ServiceDescriptor descriptor) {
        services.add(Reporter.buildResult(descriptor));
        return HealthCheck.Result.healthy();
    }

}
