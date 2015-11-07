package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public abstract class ReportHealthCheck extends HealthCheck {
    private ServiceDescriptor descriptor;
    private String domain;
    private Reporter reporter;

    ReportHealthCheck(String domain,  ServiceDescriptor descriptor, Reporter reporter) {
        this.domain = domain;
        this.descriptor = descriptor;
        this.reporter = reporter;
    }

    protected abstract Result report(Result result);

    @Override
    protected Result check() throws Exception {
        return report(Result.healthy());
    }

    public static String buildResult(ServiceDescriptor descriptor) {
        return descriptor.getHost() + ":" + descriptor.getPort();
    }

    public static ReportHealthCheck build(String domain,ServiceDescriptor descriptor,Reporter reporter) {
        return new ReportHealthCheck(domain,descriptor,reporter) {
            @Override
            protected Result report(Result result) {
                return reporter.report(result,domain,descriptor);
            }
        };
    }
}
