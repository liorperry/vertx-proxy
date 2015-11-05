package io.vertx.example.web.proxy.healthcheck;

import redis.clients.jedis.Jedis;

public class RestServiceHealthCheck extends RedisReportHealthCheck {

    public RestServiceHealthCheck(String serviceName, Jedis client) {
        super(serviceName, client);
    }

    @Override
    protected Result check() throws Exception {
        return report(Result.healthy());
    }
}
