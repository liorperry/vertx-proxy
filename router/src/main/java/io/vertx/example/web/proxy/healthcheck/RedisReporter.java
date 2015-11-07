package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import redis.clients.jedis.Jedis;

import static io.vertx.example.web.proxy.healthcheck.ReportHealthCheck.buildResult;

public class RedisReporter implements Reporter{

    private Jedis jedis;

    public RedisReporter(Jedis jedis) {
        this.jedis = jedis;
    }

    public HealthCheck.Result report(HealthCheck.Result result,String domain,ServiceDescriptor descriptor) {
        String key = domain + "." + descriptor.getServiceName();
        if (result.isHealthy()) {
            jedis.sadd(key, buildResult(descriptor));
            jedis.expire(key, 5);
        } else {
            //remove service from health services pool
            jedis.srem(key, buildResult(descriptor));
        }
        return result;
    }

}
