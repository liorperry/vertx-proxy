package io.vertx.example.unit.test.local.filter.feature;

import io.vertx.core.json.JsonArray;
import io.vertx.example.web.proxy.filter.Feature.InMemBasedStateRepository;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.NamedFeature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.buildFeatureStatus;
import static junit.framework.Assert.*;

/**
 * In Memory feature state provider
 * {
 *  {
 *     name:"Feature1",
 *     enabled:true,
 *     strategyId:"someStrategy"
 *     users: { u1,u2, u3 }
 *  },
 *  {
 *     name:"Feature2",
 *     enabled:false,
 *     strategyId:"someStrategy"
 *     users: { u1 }
 *  }
 * }
 */
@RunWith(VertxUnitRunner.class)
public class InMemBasedStateRepositoryTest {

    private InMemBasedStateRepository repository;
    private JsonArray array;

    @Before
    public void setUp() throws Exception {
        array = new JsonArray();
        array.add(buildFeatureStatus("feature2","someStrategy",false, Optional.<List<String>>empty(), Collections.emptyMap()));

    }


    @Test
    public void testStateRepositorySingleElement() {
        array.add(buildFeatureStatus("feature1","someStrategy",true,Optional.of(Arrays.asList("u1","u2","u3")), Collections.emptyMap()));
        repository = new InMemBasedStateRepository(array);
        FeatureState feature1 = repository.getFeatureState(new NamedFeature("feature1"));
        assertEquals(feature1.getFeature(), new NamedFeature("feature1"));
        assertTrue(feature1.isEnabled());
        assertEquals(feature1.getStrategyId(), "someStrategy");
        assertEquals(feature1.getUsers().size(), 3);
    }

    @Test
    public void testStateRepositoryMultipleElement() {
        array.add(buildFeatureStatus("feature1","someStrategy",true,Optional.of(Arrays.asList("u1","u2","u3")), Collections.emptyMap()));
        array.add(buildFeatureStatus("feature2","someStrategy",false,Optional.empty(), Collections.emptyMap()));
        repository = new InMemBasedStateRepository(array);
        FeatureState feature1 = repository.getFeatureState(new NamedFeature("feature1"));
        assertEquals(feature1.getFeature(),new NamedFeature("feature1"));
        assertTrue(feature1.isEnabled());
        assertEquals(feature1.getStrategyId(), "someStrategy");
        assertEquals(feature1.getUsers().size(), 3);

        FeatureState feature2 = repository.getFeatureState(new NamedFeature("feature2"));
        assertEquals(feature2.getFeature(),new NamedFeature("feature2"));
        assertFalse(feature2.isEnabled());
        assertEquals(feature2.getStrategyId(), "someStrategy");
        assertTrue(feature2.getUsers().isEmpty());

    }
}
