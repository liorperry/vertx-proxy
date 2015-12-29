package io.vertx.example.web.proxy.filter.Feature;

import io.vertx.core.json.JsonArray;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In Memory feature state provider
 * {
 *  {
 *     name:"Feature1",
 *     enabled:true,
 *     users: { u1,u2, u3 }
 *  },
 *  {
 *     name:"Feature2",
 *     enabled:false,
 *     users: { u1 }
 *  }
 * }
*/
public class InMemBasedStateRepository implements StateRepository {
    private final JsonArray data;
    private final ConcurrentHashMap<Feature,FeatureState> map;

    public InMemBasedStateRepository(JsonArray data) {
        this.data = data;
        this.map = new ConcurrentHashMap<>();

        for (int i = 0; i < data.size() ; i++) {
            map.put(new JsonFeatureState(data.getJsonObject(i)).getFeature(), new JsonFeatureState(data.getJsonObject(i)));
        }
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return map.get(feature);
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        map.put(featureState.getFeature(),featureState);
    }
}
