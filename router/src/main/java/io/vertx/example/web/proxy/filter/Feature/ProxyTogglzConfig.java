package io.vertx.example.web.proxy.filter.Feature;

import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.UserProvider;
import org.togglz.core.util.NamedFeature;

public class ProxyTogglzConfig implements TogglzConfig {
    private final StateRepository repository;
    private final UserProvider userProvider;

    public ProxyTogglzConfig(UserProvider userProvider) {
        this.repository = new InMemoryStateRepository();
        this.userProvider = userProvider;
    }

    public ProxyTogglzConfig(StateRepository repository, UserProvider userProvider) {
        this.repository = repository;
        this.userProvider = userProvider;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return NamedFeature.class;
    }

    @Override
    public StateRepository getStateRepository() {
        return repository;
    }

    @Override
    public UserProvider getUserProvider() {
        return userProvider;
    }
}
