package io.vertx.example.unit.test.local.filter.feature;

import io.vertx.core.json.JsonArray;
import io.vertx.example.web.proxy.filter.Feature.ProxyFeatureManagerProvider;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.NamedFeature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.buildFeatureMetadata;
import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.buildFeatureStatus;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

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
public class ProxyFeatureManagerProviderTest {

    private ProxyFeatureManagerProvider provider;

    private JsonArray featureArray;
    private JsonArray statusArray;

    @Before
    public void setUp() throws Exception {
        featureArray = new JsonArray();
        statusArray = new JsonArray();

        featureArray.add(buildFeatureMetadata("feature1", true, Optional.of(Arrays.asList("1", "2", "3"))));
        featureArray.add(buildFeatureMetadata("feature2", false, Optional.<List<String>>empty()));

        statusArray.add(buildFeatureStatus("feature1", "StrategyId", true, Optional.of(Arrays.asList("1", "2", "3")), Collections.emptyMap()));
        statusArray.add(buildFeatureStatus("feature2", "StrategyId", false, Optional.<List<String>>empty(), Collections.emptyMap()));

    }


    @Test
    public void testDefaultFactory() {
        provider = new ProxyFeatureManagerProvider(featureArray,statusArray);
        FeatureManager manager = provider.getFeatureManager();

        assertEquals(manager.getActivationStrategies().size(), 1);
        assertEquals(manager.getActivationStrategies().get(0).getId(), "script");

        assertEquals(manager.getFeatures().size(), 2);
        assertEquals(manager.getMetaData(new NamedFeature("feature1")).getLabel(), "feature1");
        assertEquals(manager.getMetaData(new NamedFeature("feature1")).isEnabledByDefault(), true);
        assertEquals(manager.getMetaData(new NamedFeature("feature1")).getGroups().size(),3);

        assertFalse(manager.isActive(new NamedFeature("feature2")));
        assertEquals(manager.getFeatureState(new NamedFeature("feature2")).getFeature().name(), "feature2");
        assertEquals(manager.getFeatureState(new NamedFeature("feature2")).getStrategyId(), "StrategyId");
        assertFalse(manager.getFeatureState(new NamedFeature("feature2")).isEnabled());
        assertEquals(manager.getFeatureState(new NamedFeature("feature1")).getUsers().size(),3);
        assertFalse(manager.getFeatureState(new NamedFeature("feature1")).getParameterMap().isEmpty());

    }

}
