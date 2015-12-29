package io.vertx.example.web.proxy.filter.Feature;

import io.vertx.core.json.JsonArray;
import org.togglz.core.Feature;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.spi.FeatureProvider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
public class InMemFeatureProvider implements FeatureProvider {
    private final JsonArray data;
    private final ConcurrentHashMap<Feature,FeatureMetaData> map;

    public InMemFeatureProvider(JsonArray data) {
        this.data = data;
        this.map = new ConcurrentHashMap<>();

        for (int i = 0; i < data.size() ; i++) {
            JsonFeatureMetaData metaData = new JsonFeatureMetaData(data.getJsonObject(i));
            map.put(metaData.getFeature(), metaData);
        }
    }

    @Override
    public Set<Feature> getFeatures() {
        return map.keySet().stream().collect(Collectors.toSet());
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return map.get(feature);
    }
}
