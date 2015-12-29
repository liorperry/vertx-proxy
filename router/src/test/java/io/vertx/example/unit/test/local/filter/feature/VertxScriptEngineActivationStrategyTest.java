package io.vertx.example.unit.test.local.filter.feature;

import io.vertx.core.json.JsonArray;
import io.vertx.example.web.proxy.filter.Feature.JsonFeatureState;
import io.vertx.example.web.proxy.filter.Feature.VertxScriptEngineActivationStrategy;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.togglz.core.user.SimpleFeatureUser;

import java.util.*;

import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.buildFeatureStatus;
import static io.vertx.example.web.proxy.filter.Feature.VertxScriptEngineActivationStrategy.PARAM_LANG;
import static io.vertx.example.web.proxy.filter.Feature.VertxScriptEngineActivationStrategy.PARAM_SCRIPT;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
public class VertxScriptEngineActivationStrategyTest {

    private JsonArray array;

    @Before
    public void setUp() throws Exception {
        Map<String,String> map = new HashMap<>();
        map.put(PARAM_LANG,"ECMAScript");
        map.put(PARAM_SCRIPT, "user.name == 'user1'");

        array = new JsonArray();

        array.add(buildFeatureStatus("feature1", "someStrategy", true, Optional.of(Arrays.asList("user1","user2")), map));
        array.add(buildFeatureStatus("feature2", "someStrategy", false, Optional.of(Arrays.asList("user1","user2")), map));

    }


    @Test
    public void testScriptStrategy() {
        VertxScriptEngineActivationStrategy strategy = new VertxScriptEngineActivationStrategy();
        assertTrue(strategy.isActive(new JsonFeatureState(array.getJsonObject(0)), new SimpleFeatureUser("user1")));
        assertFalse(strategy.isActive(new JsonFeatureState(array.getJsonObject(1)), new SimpleFeatureUser("user2")));
    }

}
