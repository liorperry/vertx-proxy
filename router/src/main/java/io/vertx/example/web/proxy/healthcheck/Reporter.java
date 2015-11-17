package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Closeable;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;

public interface Reporter extends Closeable {
    HealthCheck.Result report(HealthCheck.Result result,String domain,ServiceDescriptor descriptor);

    default void close(Handler<AsyncResult<Void>> completionHandler) {}

    static String buildKey(String domain, ServiceDescriptor descriptor) {
        return "\""+ domain + "." + descriptor.getKey()+"\"";
    }

    /**
     * health check reporter
     * @param vertx
     * @param domain
     * @param reporter
     * @param delayTime
     */
    static long setUpHealthCheck(Vertx vertx, String domain, VerticalServiceRegistry registry, Reporter reporter, int delayTime) {
        final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
        registry.getServices().stream().forEach(descriptor ->  healthChecks.register(descriptor.getKey(), ReportHealthCheck.build(domain, descriptor, reporter)));

        healthChecks.runHealthChecks();
        //first time health check reporting
//        vertx.runOnContext(t -> healthChecks.runHealthChecks());

        //run periodic health checks
        return vertx.setPeriodic(delayTime, t -> healthChecks.runHealthChecks());
    }
}
