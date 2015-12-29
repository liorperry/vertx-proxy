package io.vertx.example.unit.test.local.filter.feature;

import io.vertx.example.web.proxy.filter.Feature.JsonFeatureMetaData;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.util.NamedFeature;

import java.util.Arrays;
import java.util.Optional;

import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * {
 *     label:"Feature1",
 *     enabledByDefault:true,
 *     groups: { G1,G2, G3 }
 *  }
 */
@RunWith(VertxUnitRunner.class)
public class JsonFeatureMetaDataTest {


    @Test
    public void testFeatureMetadata() throws Exception {
        JsonFeatureMetaData metaData = new JsonFeatureMetaData(buildFeatureMetadata("feature1",false,Optional.of(Arrays.asList("g1", "g2", "g3"))));
        assertEquals(metaData.getFeature(), new NamedFeature("feature1"));
        assertEquals(metaData.getLabel(), "feature1");
        assertEquals(metaData.getGroups().size(), 3);
        assertFalse(metaData.getAttributes().isEmpty());
    }
}
