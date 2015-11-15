package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.netty.util.internal.ConcurrentSet;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;

import java.util.Collections;
import java.util.Set;

public class InMemReporter implements Reporter{

    private Set<ServiceDescriptor> services;

    public InMemReporter(Set<ServiceDescriptor> services) {
        this.services = services;
    }

    @Override
    public HealthCheck.Result report(HealthCheck.Result result, String domain, ServiceDescriptor descriptor) {
        services.add(descriptor);
        return HealthCheck.Result.healthy();
    }

    public Set<ServiceDescriptor> getServices() {
        return Collections.unmodifiableSet(services);
    }
}
