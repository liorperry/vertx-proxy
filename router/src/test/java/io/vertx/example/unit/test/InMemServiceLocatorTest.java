package io.vertx.example.unit.test;

import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static junit.framework.Assert.*;

@RunWith(VertxUnitRunner.class)
public class InMemServiceLocatorTest {

    private InMemServiceLocator locator;
    private Set<String> services;

    @Test
    public void serviceLocatorSingleHostTest() {
        services = new HashSet<>();
        services.add("localhost:8080");
        locator = new InMemServiceLocator("test",services, );
        Optional<String> service = locator.getService("/test/prod123");
        assertTrue(service.isPresent());
        assertEquals(service.get(), "localhost:8080");

        service = locator.getService("/test/prod321");
        assertTrue(service.isPresent());
        assertEquals(service.get(), "localhost:8080");

        service = locator.getService("/test/");
        assertTrue(service.isPresent());
        assertEquals(service.get(), "localhost:8080");
    }

    @Test
    public void serviceLocatorMultiHostsTest() {
        Map<String,Integer> map = new HashMap<>();

        services = new HashSet<>();
        services.add("localhost:8080");
        services.add("localhost1:8080");
        services.add("localhost2:8080");
        locator = new InMemServiceLocator("test",services, );

        Optional<String> service = Optional.empty();
        for (int i = 0; i < 90; i++) {
            service = locator.getService("/test/prod123");
            if(!map.containsKey(service.get())) {
                map.put(service.get(),0);
            }
            map.put(service.get(),map.get(service.get())+1);

            service = locator.getService("/test/prod321");
            if(!map.containsKey(service.get())) {
                map.put(service.get(),0);
            }
            map.put(service.get(),map.get(service.get())+1);

            service = locator.getService("/test/");
            if(!map.containsKey(service.get())) {
                map.put(service.get(),0);
            }
            map.put(service.get(),map.get(service.get())+1);
        }


        assertTrue(service.isPresent());
        assertEquals(map.get("localhost:8080").intValue(),90 );
        assertEquals(map.get("localhost1:8080").intValue(),90 );
        assertEquals(map.get("localhost2:8080").intValue(),90 );
    }
}
