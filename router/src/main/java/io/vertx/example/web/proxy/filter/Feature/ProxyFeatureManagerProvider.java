package io.vertx.example.web.proxy.filter.Feature;

import io.vertx.core.json.JsonArray;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import java.util.Collections;

public class ProxyFeatureManagerProvider implements FeatureManagerProvider {
    private final FeatureManager manager;

    /**
     * in mem feature manager
     */
    public ProxyFeatureManagerProvider(JsonArray features,
                                       JsonArray states) {
        manager = new FeatureManagerBuilder()
                .name("ProxyFeatureManagerProvider")
                .featureProvider(new InMemFeatureProvider(features))
                .stateRepository(new InMemBasedStateRepository(states))
                .userProvider(new NoOpUserProvider())
                .activationStrategyProvider(() -> Collections.singletonList(new VertxScriptEngineActivationStrategy()))
                .build();
    }

    public ProxyFeatureManagerProvider(FeatureProvider featureProvider,
                                       StateRepository repository,
                                       UserProvider userProvider) {
        manager = new FeatureManagerBuilder()
                .name("ProxyFeatureManagerProvider")
                .featureProvider(featureProvider)
                .stateRepository(repository)
                .togglzConfig(new ProxyTogglzConfig(repository, userProvider))
                .userProvider(userProvider)
                .activationStrategyProvider(() -> Collections.singletonList(new VertxScriptEngineActivationStrategy()))
                .build();
    }

    @Override
    public FeatureManager getFeatureManager() {
        return manager;
    }

    @Override
    public int priority() {
        return 0;
    }
}
