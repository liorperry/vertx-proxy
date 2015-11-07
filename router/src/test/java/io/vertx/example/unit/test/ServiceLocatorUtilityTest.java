package io.vertx.example.unit.test;

import io.vertx.example.web.proxy.locator.ServiceLocator;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class ServiceLocatorUtilityTest {

    @Test
    public void serviceLocatorUtilityPortTest() {
        int port = ServiceLocator.getPort("localhost:8080");
        assertEquals(port,8080);

        port = ServiceLocator.getPort("www.localhost:8080");
        assertEquals(port,8080);

        port = ServiceLocator.getPort("127.0.0.1:8080");
        assertEquals(port,8080);
    }

    @Test
    public void serviceLocatorUtilityHostTest() {
        String host = ServiceLocator.getHost("localhost:8080");
        assertEquals(host, "localhost");

        host = ServiceLocator.getHost("127.0.0.1:8080");
        assertEquals(host, "127.0.0.1");

        host = ServiceLocator.getHost("www.localhost:8080");
        assertEquals(host, "www.localhost");
    }
}
