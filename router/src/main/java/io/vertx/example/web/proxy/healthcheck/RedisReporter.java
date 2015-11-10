package io.vertx.example.web.proxy.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import redis.clients.jedis.Jedis;


public class RedisReporter implements Reporter{

    private Jedis jedis;
    private int expirationTime = 5;

    public RedisReporter(Jedis jedis, int expirationTime) {
        this.jedis = jedis;
        this.expirationTime = expirationTime;
    }

    public HealthCheck.Result report(HealthCheck.Result result,String domain,ServiceDescriptor descriptor) {
        String key = Reporter.buildKey(domain, descriptor);
        if (result.isHealthy()) {
            jedis.set(key, Reporter.buildResult(descriptor));
            jedis.expire(key, expirationTime);
            System.out.println("Reporting live ["+key+"] :"+ Reporter.buildResult(descriptor));
        } else {
            //remove service from health services pool
            jedis.del(key, Reporter.buildResult(descriptor));
        }
        return result;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        if(jedis.isConnected()) {
            jedis.close();
        }
    }
}
