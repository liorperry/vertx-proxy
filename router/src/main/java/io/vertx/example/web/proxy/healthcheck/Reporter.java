package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Closeable;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.ServiceRegistry;

public interface Reporter extends Closeable {
    HealthCheck.Result report(HealthCheck.Result result,String domain,ServiceDescriptor descriptor);

    /**
     * health check reporter
     * @param vertx
     * @param domain
     * @param reporter
     * @return
     */
    static void setUpHealthCheck(Vertx vertx,String domain, ServiceRegistry registry,Reporter reporter) {
        final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
        for (ServiceDescriptor descriptor : registry.getServices()) {
            healthChecks.register(descriptor.getServiceName(), ReportHealthCheck.build(domain, descriptor, reporter));
        }
        //first time health check reporting
        vertx.runOnContext(t -> healthChecks.runHealthChecks());
        //run periodic health checks
        vertx.setPeriodic(2000, t -> healthChecks.runHealthChecks());
    }
}
