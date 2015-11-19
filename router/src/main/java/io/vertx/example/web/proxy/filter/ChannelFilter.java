package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.KeysRepository;

import java.util.Optional;

import static io.vertx.example.web.proxy.filter.FilterUtils.extractService;

public class ChannelFilter implements FilterPhase {
    @Override
    public boolean filter(HttpServerRequest request, KeysRepository keysRepository) {
        String uri = request.uri();
        Optional<String> service = extractService(uri);
        String channel = request.getHeader("channel");
        Optional<Boolean> response = keysRepository.getChannelService(uri, channel);
        System.out.println("filter uri :" + uri + " channel "+channel +" service " + extractService(uri) + ">>" + response);
        return service.isPresent() && response.isPresent() && response.get();
    }
}
