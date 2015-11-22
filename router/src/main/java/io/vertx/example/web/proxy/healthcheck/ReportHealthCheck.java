package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;

public abstract class ReportHealthCheck extends HealthCheck {
    private ServiceDescriptor descriptor;
    private String domain;
    private HealthReporter healthReporter;

    ReportHealthCheck(String domain,  ServiceDescriptor descriptor, HealthReporter healthReporter) {
        this.domain = domain;
        this.descriptor = descriptor;
        this.healthReporter = healthReporter;
    }

    protected abstract Result report(Result result);

    @Override
    protected Result check() throws Exception {
        return report(Result.healthy());
    }

    public static ReportHealthCheck build(String domain,ServiceDescriptor descriptor,HealthReporter healthReporter) {
        return new ReportHealthCheck(domain,descriptor, healthReporter) {
            @Override
            protected Result report(Result result) {
                return healthReporter.report(result,domain,descriptor);
            }
        };
    }
}
