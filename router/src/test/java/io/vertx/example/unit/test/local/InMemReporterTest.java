package io.vertx.example.unit.test.local;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.example.web.proxy.healthcheck.InMemReporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class InMemReporterTest {

    @Test
    public void testInMemReporter() throws Exception {
        InMemReporter reporter = new InMemReporter(new VerticalServiceRegistry());
        reporter.report(HealthCheck.Result.healthy(), "domain",ServiceDescriptor.create("testService"));
        assertEquals(reporter.getServices().size(),1);
        assertEquals(reporter.getServices().iterator().next(),ServiceDescriptor.create("testService"));
    }
}
