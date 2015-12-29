package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.KeysRepository;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.NamedFeature;

import java.util.Optional;

import static io.vertx.example.web.proxy.filter.FilterUtils.extractService;

public class GenericScriptFilter implements FilterPhase {
    private FeatureManager featureManager;

    public GenericScriptFilter(FeatureManager featureManager ) {
        this.featureManager = featureManager;
    }

    @Override
    public boolean filter(HttpServerRequest request, KeysRepository keysRepository) {
        String uri = request.uri();
        Optional<String> service = extractService(uri);
        return !service.isPresent() || featureManager.isActive(new NamedFeature(service.get()));
    }
}
