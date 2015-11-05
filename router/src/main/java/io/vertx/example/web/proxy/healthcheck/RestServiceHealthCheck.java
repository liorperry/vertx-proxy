package io.vertx.example.web.proxy.healthcheck;

import redis.clients.jedis.Jedis;

public class RestServiceHealthCheck extends RedisReportHealthCheck {

    public RestServiceHealthCheck(ServiceDescriptor serviceDescriptor, Jedis client) {
        super(serviceDescriptor, client);
    }

    @Override
    protected Result check() throws Exception {
        return report(Result.healthy());
    }
}
