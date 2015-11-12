package io.vertx.example.unit.test.local;

import io.vertx.example.web.proxy.locator.RoundRobinPool;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.ServiceVersion;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static junit.framework.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class RoundRobinTest {
    public static final String SERVICE_A = "serviceA";
    private static final String SERVICE_B = "serviceB";

    public static final int COUNTER = 10000;

    public static final String ELEMENT_1 = "element 1";
    public static final String ELEMENT_2 = "element 2";
    public static final String ELEMENT_3 = "element 3";
    public static final String ELEMENT_4 = "element 4";
    public static final String ELEMENT_5 = "element 5";

    Map<ServiceDescriptor, Integer> serviceAActivations;
    Map<ServiceDescriptor, Integer> serviceBActivations;

    @Before
    public void setUp() throws Exception {

        serviceAActivations = new HashMap<>();
        serviceBActivations = new HashMap<>();
    }

    @Test
    public void roundRobinPoolSimpleTest() {
        RoundRobinPool pool = new RoundRobinPool();
        populatePool(pool,SERVICE_A );
        assertEquals(pool.size(), 1);
        assertEquals(pool.size(new ServiceVersion(SERVICE_A, "1")), 4);
        populatePool(pool, SERVICE_B);
        assertEquals(pool.size(new ServiceVersion(SERVICE_B, "1")), 4);
        assertEquals(pool.size(), 2);

        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>empty(), new ServiceVersion(SERVICE_A,"1"));
        iterate(pool, serviceBActivations, Optional.<Activity<RoundRobinPool>>empty(), new ServiceVersion(SERVICE_B,"1"));

        assertEquals(serviceAActivations.size(), 4);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080)).intValue(), 2500);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_2, 8080)).intValue(), 2500);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_3, 8080)).intValue(), 2500);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_4, 8080)).intValue(), 2500);

        assertEquals(serviceBActivations.size(), 4);
        assertEquals(serviceBActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_B, "1"), ELEMENT_1, 8080)).intValue(), 2500);
        assertEquals(serviceBActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_B, "1"), ELEMENT_2, 8080)).intValue(), 2500);
        assertEquals(serviceBActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_B, "1"), ELEMENT_3, 8080)).intValue(), 2500);
        assertEquals(serviceBActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_B, "1"), ELEMENT_4, 8080)).intValue(), 2500);
    }

    private void populatePool(RoundRobinPool pool, String serviceName) {
        pool.addService(ServiceDescriptor.create(new ServiceVersion(serviceName, "1"), ELEMENT_1, 8080));
        pool.addService(ServiceDescriptor.create(new ServiceVersion(serviceName, "1"), ELEMENT_2, 8080));
        pool.addService(ServiceDescriptor.create(new ServiceVersion(serviceName, "1"), ELEMENT_3, 8080));
        pool.addService(ServiceDescriptor.create(new ServiceVersion(serviceName, "1"), ELEMENT_4, 8080));
    }

    @Test
    public void roundRobinPoolWithRemovalTest() {
        RoundRobinPool pool = new RoundRobinPool();
        populatePool(pool,SERVICE_A );
        assertEquals(pool.size(new ServiceVersion(SERVICE_A, "1")), 4);
        //validate pool has a set of unique service descriptors
        populatePool(pool, SERVICE_A);
        assertEquals(pool.size(new ServiceVersion(SERVICE_A, "1")), 4);


        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>of((element, index) -> {
            if (index % 5020 == 0) {
                element.removeService(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080));
            }
        }), new ServiceVersion(SERVICE_A,"1"));

        assertEquals(serviceAActivations.size(), 4);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080)).intValue(), 1255);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_2, 8080)).intValue(), 2915);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_3, 8080)).intValue(), 2915);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_4, 8080)).intValue(), 2915);

    }

    @Test
    public void roundRobinPoolWithAddTest() {
        RoundRobinPool pool = new RoundRobinPool();
        populatePool(pool, SERVICE_A);
        assertEquals(pool.size(new ServiceVersion(SERVICE_A, "1")), 4);

        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>of((element, index) -> {
            if (index % 5000 == 0) {
                pool.addService(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_5, 8080));
            }
        }), new ServiceVersion(SERVICE_A, "1"));

        assertEquals(pool.size(new ServiceVersion(SERVICE_A, "1")), 5);

        assertEquals(serviceAActivations.size(), 5);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080)).intValue(), 2250);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_2, 8080)).intValue(), 2250);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_3, 8080)).intValue(), 2250);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_4, 8080)).intValue(), 2250);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_5, 8080)).intValue(), 1000);

    }

    @Test
    public void roundRobinPoolWithExclusionTest() {
        Set<ServiceDescriptor> results = new HashSet<>();
        RoundRobinPool pool = new RoundRobinPool();
        populatePool(pool, SERVICE_A);

        Collection<ServiceDescriptor> all = pool.getAll(new ServiceVersion(SERVICE_A, "1"));
        HashSet<ServiceDescriptor> exclusionList = new HashSet<>(all);
        exclusionList.remove(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080));

        Optional<ServiceDescriptor> result = pool.get(new ServiceVersion(SERVICE_A, "1"), exclusionList);
        assertEquals(result.get(), ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080));

        exclusionList.remove(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_2, 8080));
        results.add(pool.get(new ServiceVersion(SERVICE_A, "1"), exclusionList).get());
        results.add(pool.get(new ServiceVersion(SERVICE_A, "1"), exclusionList).get());

        assertEquals(results.size(),2);


    }


    public void roundRobinPoolWithAddRemoveTest() {
        RoundRobinPool pool = new RoundRobinPool();
        populatePool(pool, SERVICE_A);


        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>of((element, index) -> {
            if (index % 5000 == 0) {
                element.addService(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_5, 8080));
            }
            if (index % 7500 == 0) {
                element.removeService(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080));
            }
        }), new ServiceVersion(SERVICE_A,"1"));

        assertEquals(serviceAActivations.size(), 5);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_1, 8080)).intValue(), 1750);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_2, 8080)).intValue(), 2375);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_3, 8080)).intValue(), 2375);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_4, 8080)).intValue(), 2375);
        assertEquals(serviceAActivations.get(ServiceDescriptor.create(new ServiceVersion(SERVICE_A, "1"), ELEMENT_5, 8080)).intValue(), 1125);

    }

    private void iterate(RoundRobinPool pool, Map<ServiceDescriptor,Integer> map, Optional<Activity<RoundRobinPool>> activity, ServiceVersion serviceName) {
        for (int i = 1; i < COUNTER+1; i++) {
            Optional<ServiceDescriptor> response = pool.get(serviceName);
            if (response.isPresent()) {
                if (activity.isPresent()) {
                    activity.get().doActivity(pool,i);
                }
                ServiceDescriptor key = response.get();
                if (!map.containsKey(key)) {
                    map.put(key, 0);
                }
                map.put(key, map.get(key) + 1);
            }
        }
    }

    interface Activity<T> {
        void doActivity(T element,int index);
    }
}
