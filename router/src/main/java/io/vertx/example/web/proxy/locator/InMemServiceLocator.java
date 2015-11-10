package io.vertx.example.web.proxy.locator;

import io.vertx.example.web.proxy.filter.FilterUtils;

import java.util.*;

public class InMemServiceLocator implements ServiceLocator {
    private String domain;
    private Map<String, Set<String>> servicesLocations;
    private Map<String, HashSet<String>> servicesBlockedLocations;
    private RoundRobinPool pool;

    public InMemServiceLocator(String domain, Map<String, Set<String>> servicesLocations) {
        this.servicesBlockedLocations = new HashMap<>();
        this.domain = domain;
        this.servicesLocations = servicesLocations;
        this.pool = new RoundRobinPool();
        //update keys in pool - if absent
        this.pool.addServices(Collections.unmodifiableMap(servicesLocations));
    }

    @Override
    public Optional<String> getService(String uri) {
        System.out.println("******* InMemServiceLocator :: *****************");
        Optional<String> service = FilterUtils.extractService(uri);
        if (!service.isPresent()) {
            System.out.println("InMemServiceLocator:: return empty");
            return Optional.empty();
        }

        //reload pool if services where removed/added
        if (servicesLocations.get(service.get()).size() != pool.getAll(service.get()).size()) {
            pool.updateService(service.get(), servicesLocations.get(service.get()));
        }

        if (!servicesBlockedLocations.containsKey(service.get())) {
            //get next (circular loop) round robin
            Optional<String> result = pool.get(service.get());
            System.out.println("InMemServiceLocator::result" + result);
            return result;
        }

        HashSet<String> excludeList = servicesBlockedLocations.get(service.get());
        System.out.println("InMemServiceLocator::servicesBlockedLocations");
        excludeList.stream().forEach(System.out::println);
        Optional<String> result = pool.get(service.get(), excludeList);
        System.out.println("InMemServiceLocator::result" + result);
        return result;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public Collection<String> getAllProviders(String serviceName) {
        return Collections.unmodifiableCollection(pool.getAll(serviceName));
    }

    public void blockServiceProvider(String service, String address) {
        if (!servicesBlockedLocations.containsKey(service)) {
            servicesBlockedLocations.put(service, new HashSet<>());
        }
        servicesBlockedLocations.get(service).add(address);
    }

    public static InMemServiceLocator create(String domain, Map<String, Set<String>> servicesLocations) {
        return new InMemServiceLocator(domain, servicesLocations);
    }
}

