package io.vertx.example.web.proxy.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter responsible for apply the service verification gateway
 */
public class Filter {
    private Repository repository;
    private List<FilterPhase> chain;

    private Filter(Repository repository) {
        this.chain = new ArrayList<>();
        this.repository = repository;
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
            if(!filterPhase.filter(request,repository )) {
                return false;
            }
        }
        return true;
    }
    
    public static class FilterBuilder {
        private Filter filter;

        FilterBuilder(Repository repository) {
            filter = new Filter(repository);
        }

        public static FilterBuilder filterBuilder(Repository repository) {
            return new FilterBuilder(repository);
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
