package io.vertx.example.unit.test.local.filter.feature;

import io.vertx.core.json.JsonArray;
import io.vertx.example.web.proxy.filter.Feature.InMemFeatureProvider;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.util.NamedFeature;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.buildFeatureMetadata;
import static junit.framework.Assert.*;

/**
 * In Memory feature metadata provider
 * {
 *  {
 *     label:"Feature1",
 *     enabledByDefault:true,
 *     groups: { G1,G2, G3 }
 *  },
 *  {
 *     label:"Feature2",
 *     enabledByDefault:false,
 *     groups: { G3 }
 *  }
 * }
 */
@RunWith(VertxUnitRunner.class)
public class InMemFeatureProviderTest {

    private InMemFeatureProvider provider;
    private JsonArray array;

    @Before
    public void setUp() throws Exception {
        array = new JsonArray();
        array.add(buildFeatureMetadata("feature2", false, Optional.<List<String>>empty()));

    }

    @Test
    public void testStateRepositorySingleElement() {
        array.add(buildFeatureMetadata("feature1", true, Optional.of(Arrays.asList("u1", "u2", "u3"))));
        provider = new InMemFeatureProvider(array);
        FeatureMetaData feature1 = provider.getMetaData(new NamedFeature("feature1"));
        assertEquals(feature1.getLabel(), "feature1");
        assertTrue(feature1.isEnabledByDefault());
        assertEquals(feature1.getGroups().size(), 3);
    }

    @Test
    public void testStateRepositoryMultipleElement() {
        array.add(buildFeatureMetadata("feature1", true, Optional.of(Arrays.asList("u1", "u2", "u3"))));
        array.add(buildFeatureMetadata("feature2", false, Optional.empty()));
        provider = new InMemFeatureProvider(array);
        FeatureMetaData feature1 = provider.getMetaData(new NamedFeature("feature1"));
        assertEquals(feature1.getLabel(), "feature1");
        assertTrue(feature1.isEnabledByDefault());
        assertEquals(feature1.getGroups().size(), 3);

        FeatureMetaData feature2 = provider.getMetaData(new NamedFeature("feature2"));
        assertEquals(feature2.getLabel(),"feature2");
        assertFalse(feature2.isEnabledByDefault());
        assertTrue(feature2.getGroups().isEmpty());
    }
}
