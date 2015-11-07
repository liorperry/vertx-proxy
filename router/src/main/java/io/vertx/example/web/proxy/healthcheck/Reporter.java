package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public interface Reporter {
    HealthCheck.Result report(HealthCheck.Result result,String domain,ServiceDescriptor descriptor);
}
