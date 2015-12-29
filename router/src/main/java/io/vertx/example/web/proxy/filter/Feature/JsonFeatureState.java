package io.vertx.example.web.proxy.filter.Feature;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.NamedFeature;

import java.util.*;

import static org.togglz.core.activation.UsernameActivationStrategy.PARAM_USERS;

/**
 * JsonFeatureState represents json based feature
 *  {
 *     name:"Feature1",
 *     enabled:true,
 *     strategyId:"myStrategy"
 *     users: { u1,u2, u3 }
 *  }
 */
public class JsonFeatureState extends FeatureState {
    private final JsonObject object;

    public JsonFeatureState(JsonObject object) {
        super(new NamedFeature(object.getString("name")));
        this.object = object;

        this.setEnabled(object.getBoolean("enabled"));
        this.setStrategyId(object.getString("strategyId"));
        //populate param data
        this.populateUsers();
        this.populateParams();
    }

    private void populateParams() {
        getAttributes().entrySet().stream().forEach(entry -> {setParameter(entry.getKey(),entry.getValue());});
    }


    private void populateUsers() {
        JsonArray array = object.getJsonArray(PARAM_USERS);
        if(array==null)
            return;

        StringBuilder usersStr = new StringBuilder();
        //iterate over group list & extract simple feature group
        for (int i = 0; i < array.size(); i++) {
            usersStr.append(array.getString(i)).append(",");
        }
        usersStr.deleteCharAt(usersStr.lastIndexOf(","));
        setParameter(PARAM_USERS,usersStr.toString());
    }


    public Map<String, String> getAttributes() {
        Map<String, String> properties = new HashMap<>();
        object.getMap().entrySet().forEach(stringObjectEntry -> properties.put(stringObjectEntry.getKey(),stringObjectEntry.getValue().toString()));
        return properties;
    }

}
