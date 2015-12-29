package io.vertx.example.web.proxy.filter.Feature;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.SimpleFeatureGroup;
import org.togglz.core.util.NamedFeature;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JsonFeatureMetaData represents json based feature metadata
 *  {
 *     label:"Feature1",
 *     enabledByDefault:true,
 *     groups: { G1,G2, G3 }
 *  }
 */
public class JsonFeatureMetaData implements FeatureMetaData {
    private final JsonObject object;
    private Feature feature;

    public JsonFeatureMetaData(JsonObject object) {
        this.object = object;
        this.feature = new NamedFeature(object.getString("label"));
    }

    public Feature getFeature() {
        return feature;
    }

    @Override
    public String getLabel() {
        return object.getString("label");
    }

    @Override
    public boolean isEnabledByDefault() {
        return object.getBoolean("enabledByDefault");
    }

    @Override
    public Set<FeatureGroup> getGroups() {
        Set<FeatureGroup> groups = new HashSet<>();
        JsonArray array = object.getJsonArray("groups");
        if(array==null) {
            return Collections.emptySet();
        }

        //iterate over group list & extract simple feature group
        for (int i = 0; i < array.size(); i++) {
            groups.add(new SimpleFeatureGroup(array.getString(i)));
        }
        return groups;
    }

    @Override
    public Map<String, String> getAttributes() {
        return object.getMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonFeatureMetaData)) return false;

        JsonFeatureMetaData that = (JsonFeatureMetaData) o;

        return feature.equals(that.feature);

    }

    @Override
    public int hashCode() {
        return feature.hashCode();
    }
}
