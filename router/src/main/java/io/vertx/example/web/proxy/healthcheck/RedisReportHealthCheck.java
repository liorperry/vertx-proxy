package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class RedisReportHealthCheck extends HealthCheck {
    public static final String DOMAIN = "services";
    private final Jedis jedis;
    private ServiceDescriptor descriptor;

    public RedisReportHealthCheck(ServiceDescriptor serviceDescriptor, Jedis client) {
        this.jedis = client;
        this.descriptor = serviceDescriptor;
    }

    protected Result report(Result result) {
        String key = DOMAIN + "." + descriptor.getServiceName();
        if (result.isHealthy()) {
            jedis.sadd(key, buildResult(result));
            jedis.expire(key, 5);
        }
        return result;
    }

    private String buildResult(Result result) {
        return descriptor.getHost() + ":" + descriptor.getPort();
    }

}
