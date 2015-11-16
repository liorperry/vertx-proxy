package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Sets;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;

import java.util.Set;

public class InMemReporter implements Reporter{

    private VerticalServiceRegistry registry ;

    public InMemReporter(VerticalServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public HealthCheck.Result report(HealthCheck.Result result, String domain, ServiceDescriptor descriptor) {
        registry.register(descriptor);
        return HealthCheck.Result.healthy();
    }

    public Set<ServiceDescriptor> getServices() {
        return Sets.newHashSet(registry.getServices());
    }
}
