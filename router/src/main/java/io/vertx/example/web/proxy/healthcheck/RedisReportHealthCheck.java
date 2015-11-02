package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import redis.clients.jedis.Jedis;

public abstract class RedisReportHealthCheck extends HealthCheck{
    public static final String DOMAIN = "health-checks";
    private final Jedis jedis;
    private String key ;

    public RedisReportHealthCheck(String name) {
        this.jedis = new Jedis("localhost");
        this.key = DOMAIN + "." + name;
    }

    protected Result report(Result result) {
        jedis.set(key, Boolean.toString(result.isHealthy()));
        jedis.expire(key, 5);
        return result;
    }

}
