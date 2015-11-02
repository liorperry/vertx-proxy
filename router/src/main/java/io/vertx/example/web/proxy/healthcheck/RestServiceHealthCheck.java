package io.vertx.example.web.proxy.healthcheck;

public class RestServiceHealthCheck extends RedisReportHealthCheck {

    public RestServiceHealthCheck(String serviceName) {
        super(serviceName);
    }

    @Override
    protected Result check() throws Exception {
        return report(Result.healthy());
    }
}
