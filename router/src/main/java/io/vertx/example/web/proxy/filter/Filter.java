package io.vertx.example.web.proxy.filter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.Closeable;
import io.vertx.example.web.proxy.repository.KeysRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter responsible for apply the service verification gateway
 */
public class Filter implements Closeable{
    private KeysRepository keysRepository;
    private List<FilterPhase> chain;

    private Filter(KeysRepository keysRepository) {
        this.chain = new ArrayList<>();
        this.keysRepository = keysRepository;
    }

    private void addPhase(FilterPhase phase) {
        chain.add(phase);
    }

    /**
     * apply request based filter -
     *  - uri
     *  - request params
     *  - headers
     * @param request
     * @return
     */
    public boolean filter(HttpServerRequest request) {
        for (FilterPhase filterPhase : chain) {
            if(!filterPhase.filter(request, keysRepository)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        chain.clear();
        keysRepository.close(completionHandler);
    }

    public static class FilterBuilder {
        private Filter filter;

        FilterBuilder(KeysRepository keysRepository) {
            filter = new Filter(keysRepository);
        }

        public static FilterBuilder filterBuilder(KeysRepository keysRepository) {
            return new FilterBuilder(keysRepository);
        }
        
        public FilterBuilder add(FilterPhase phase) {
            filter.addPhase(phase);
            return this;
        }

        public Filter build() {
            return filter;
        }
    }
}
