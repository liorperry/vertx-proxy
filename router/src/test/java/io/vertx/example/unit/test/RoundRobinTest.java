package io.vertx.example.unit.test;

import io.vertx.example.web.proxy.locator.RoundRobinPool;
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

    Map<String, Integer> serviceAActivations;
    Map<String, Integer> serviceBActivations;
    private Set setA;
    private Set setB;

    @Before
    public void setUp() throws Exception {
        setA = new HashSet();
        setA.add(ELEMENT_1);
        setA.add(ELEMENT_2);
        setA.add(ELEMENT_3);
        setA.add(ELEMENT_4);

        setB = new HashSet();
        setB.add(ELEMENT_1);
        setB.add(ELEMENT_2);
        setB.add(ELEMENT_3);
        setB.add(ELEMENT_4);

        serviceAActivations = new HashMap<>();
        serviceBActivations = new HashMap<>();
    }

    @Test
    public void roundRobinPoolSimpleTest() {
        RoundRobinPool pool = new RoundRobinPool();
        pool.addServices(SERVICE_A, setA);
        pool.addServices(SERVICE_B, setB);

        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>empty(), SERVICE_A);
        iterate(pool, serviceBActivations, Optional.<Activity<RoundRobinPool>>empty(), SERVICE_B);

        assertEquals(serviceAActivations.size(), 4);
        assertEquals(serviceAActivations.get(ELEMENT_1).intValue(), 2500);
        assertEquals(serviceAActivations.get(ELEMENT_2).intValue(), 2500);
        assertEquals(serviceAActivations.get(ELEMENT_3).intValue(), 2500);
        assertEquals(serviceAActivations.get(ELEMENT_4).intValue(), 2500);

        assertEquals(serviceBActivations.size(), 4);
        assertEquals(serviceBActivations.get(ELEMENT_1).intValue(), 2500);
        assertEquals(serviceBActivations.get(ELEMENT_2).intValue(), 2500);
        assertEquals(serviceBActivations.get(ELEMENT_3).intValue(), 2500);
        assertEquals(serviceBActivations.get(ELEMENT_4).intValue(), 2500);
    }

    @Test
    public void roundRobinPoolWithRemovalTest() {
        RoundRobinPool pool = new RoundRobinPool();
        pool.addServices(SERVICE_A, setA);

        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>of((element, index) -> {
            if (index % 5020 == 0) {
                element.removeService(SERVICE_A, ELEMENT_1);
            }
        }), SERVICE_A);

        assertEquals(serviceAActivations.size(), 4);
        assertEquals(serviceAActivations.get(ELEMENT_1).intValue(), 1255);
        assertEquals(serviceAActivations.get(ELEMENT_2).intValue(), 2915);
        assertEquals(serviceAActivations.get(ELEMENT_3).intValue(), 2915);
        assertEquals(serviceAActivations.get(ELEMENT_4).intValue(), 2915);

    }

    @Test
    public void roundRobinPoolWithAddTest() {
        RoundRobinPool pool = new RoundRobinPool();
        pool.addServices(SERVICE_A, setA);

        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>of((element, index) -> {
            if (index % 5000 == 0) {
                element.addService(SERVICE_A, ELEMENT_5);
            }
        }), SERVICE_A);

        assertEquals(serviceAActivations.size(), 5);
        assertEquals(serviceAActivations.get(ELEMENT_1).intValue(), 2250);
        assertEquals(serviceAActivations.get(ELEMENT_2).intValue(), 2250);
        assertEquals(serviceAActivations.get(ELEMENT_3).intValue(), 2250);
        assertEquals(serviceAActivations.get(ELEMENT_4).intValue(), 2250);
        assertEquals(serviceAActivations.get(ELEMENT_5).intValue(), 1000);

    }
    @Test
    public void roundRobinPoolWithAddRemoveTest() {
        RoundRobinPool pool = new RoundRobinPool();
        pool.addServices(SERVICE_A, setA);

        iterate(pool, serviceAActivations, Optional.<Activity<RoundRobinPool>>of((element, index) -> {
            if (index % 5000 == 0) {
                element.addService(SERVICE_A, ELEMENT_5);
            }
            if (index % 7500 == 0) {
                element.removeService(SERVICE_A, ELEMENT_1);
            }
        }), SERVICE_A);

        assertEquals(serviceAActivations.size(), 5);
        assertEquals(serviceAActivations.get(ELEMENT_1).intValue(), 1750);
        assertEquals(serviceAActivations.get(ELEMENT_2).intValue(), 2375);
        assertEquals(serviceAActivations.get(ELEMENT_3).intValue(), 2375);
        assertEquals(serviceAActivations.get(ELEMENT_4).intValue(), 2375);
        assertEquals(serviceAActivations.get(ELEMENT_5).intValue(), 1125);

    }

    private void iterate(RoundRobinPool pool, Map<String, Integer> map, Optional<Activity<RoundRobinPool>> activity, String serviceName) {
        for (int i = 1; i < COUNTER+1; i++) {
            Optional<String> response = pool.get(serviceName);
            if (response.isPresent()) {
                if (activity.isPresent()) {
                    activity.get().doActivity(pool,i);
                }
                String key = response.get();
                if (!map.containsKey(key)) {
                    map.put(key, 0);
                }
                map.put(key, map.get(key) + 1);
            }
        }
    }

    static interface Activity<T> {
        void doActivity(T element,int index);
    }
}
