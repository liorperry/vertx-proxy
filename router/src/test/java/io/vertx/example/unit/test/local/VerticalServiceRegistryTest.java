package io.vertx.example.unit.test.local;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.example.web.proxy.healthcheck.InMemReporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static junit.framework.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class VerticalServiceRegistryTest {

    @Test
    public void testRegister() throws Exception {
        VerticalServiceRegistry registry = new VerticalServiceRegistry();
        registry.register(ServiceDescriptor.create("testService"));
        assertEquals(registry.getServices().size(),1);
        assertEquals(registry.getServices().iterator().next(), ServiceDescriptor.create("testService"));

    }

    @Test
    public void testUnRegister() throws Exception {
        VerticalServiceRegistry registry = new VerticalServiceRegistry();
        registry.register(ServiceDescriptor.create("testService"));
        assertEquals(registry.getServices().size(), 1);
        assertEquals(registry.getServices().iterator().next(), ServiceDescriptor.create("testService"));
        registry.unregister(ServiceDescriptor.create("testService"));
        assertEquals(registry.getServices().size(), 0);

    }

    @Test
    public void testClose() throws Exception {
        VerticalServiceRegistry registry = new VerticalServiceRegistry();
        registry.register(ServiceDescriptor.create("testService"));
        assertEquals(registry.getServices().size(), 1);
        assertEquals(registry.getServices().iterator().next(), ServiceDescriptor.create("testService"));
        registry.close(event -> {});
        assertEquals(registry.getServices().size(), 0);

    }
}
