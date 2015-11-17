package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;


public class RedisReporter implements Reporter{

    private JedisPool pool;
    private int expirationTime = 5;

    public RedisReporter(JedisPool pool, int expirationTime) {
        this.pool = pool;
        this.expirationTime = expirationTime;
    }

    public HealthCheck.Result report(HealthCheck.Result result,String domain,ServiceDescriptor descriptor) {
        String key = Reporter.buildKey(domain, descriptor);
        String value = descriptor.getAsJsonKey().encode();
        Jedis jedis = pool.getResource();

        try {
            if (result.isHealthy()) {
                System.out.println("Reporting live [" + key + "] :" + value);
                jedis.set(key, value);
                jedis.expire(key, expirationTime);
            } else {
                //remove service from health services pool
                jedis.del(key, value);
            }
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return result;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        pool.close();
    }
}
