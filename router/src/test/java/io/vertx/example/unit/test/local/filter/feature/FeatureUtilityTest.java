package io.vertx.example.unit.test.local.filter.feature;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class FeatureUtilityTest {

    public static JsonObject buildFeatureMetadata(String name, boolean enabled, Optional<List<String>> groups) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("label", name);
        map.put("enabledByDefault",enabled);
        if(groups.isPresent()) {
            JsonArray array = new JsonArray();
            groups.get().stream().forEach(array::add);
            map.put("groups", array);
        }
        return new JsonObject(map);
    }

    public static JsonObject buildFeatureStatus(String name,String strategyId, boolean enabled,Optional<List<String>> users,Map<String,String> params) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("strategyId", strategyId);
        map.put("enabled",enabled);
        if(users.isPresent()) {
            JsonArray array = new JsonArray();
            users.get().stream().forEach(array::add);
            map.put("users", array);
        }
        if(!map.isEmpty()) {
            map.putAll(params);
        }
        return new JsonObject(map);
    }

}
