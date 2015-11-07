package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import redis.clients.jedis.Jedis;

import static io.vertx.example.web.proxy.healthcheck.ReportHealthCheck.buildResult;

public class RedisReporter implements Reporter{

    private Jedis jedis;
    private int expirationTime = 5;

    public RedisReporter(Jedis jedis, int expirationTime) {
        this.jedis = jedis;
        this.expirationTime = expirationTime;
    }

    public HealthCheck.Result report(HealthCheck.Result result,String domain,ServiceDescriptor descriptor) {
        String key = domain + "." + descriptor.getServiceName() + "@" +descriptor.getUuid();
        if (result.isHealthy()) {
            jedis.set(key, buildResult(descriptor));
            jedis.expire(key, expirationTime);
            System.out.println("Reporting live ["+key+"] :"+buildResult(descriptor));
        } else {
            //remove service from health services pool
            jedis.del(key, buildResult(descriptor));
        }
        return result;
    }

}
