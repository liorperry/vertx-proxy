package io.vertx.example.unit.test.local;

import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class InMemServiceLocatorTest {

    public static final String DOMAIN = "test";
    public static final String SERVICE = "service";
    private InMemServiceLocator locator;
    private Set<String> servicesLocations;

    @Test
    public void serviceLocatorSingleHostTest() {
        servicesLocations = new HashSet<>();
        servicesLocations.add("localhost:8080");
        locator = new InMemServiceLocator(DOMAIN,
                Collections.singletonMap(SERVICE,servicesLocations)
        );

        Optional<String> service = locator.getService("/service/prod1");
        assertTrue(service.isPresent());
        assertEquals(service.get(), "localhost:8080");

        service = locator.getService("/service/prod1");
        assertTrue(service.isPresent());
        assertEquals(service.get(), "localhost:8080");

        service = locator.getService("/service/");
        assertTrue(service.isPresent());
        assertEquals(service.get(), "localhost:8080");
    }

    @Test
    public void serviceLocatorMultiHostsTest() {
        Map<String,Integer> map = new HashMap<>();

        servicesLocations = new HashSet<>();
        servicesLocations.add("localhost:8080");
        servicesLocations.add("localhost1:8080");
        servicesLocations.add("localhost2:8080");

        locator = new InMemServiceLocator(DOMAIN,
                Collections.singletonMap(SERVICE,servicesLocations)
        );

        Optional<String> service = Optional.empty();
        for (int i = 0; i < 90; i++) {
            service = locator.getService("/service/prod1");
            if(!map.containsKey(service.get())) {
                map.put(service.get(),0);
            }
            map.put(service.get(),map.get(service.get())+1);

            service = locator.getService("/service/prod2");
            if(!map.containsKey(service.get())) {
                map.put(service.get(),0);
            }
            map.put(service.get(),map.get(service.get())+1);

            service = locator.getService("/service/");
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
