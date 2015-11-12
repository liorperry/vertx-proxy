package io.vertx.example.web.proxy.locator;

import io.netty.util.internal.ConcurrentSet;
import io.vertx.example.web.proxy.filter.FilterUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class InMemServiceLocator implements ServiceLocator {
    private String domain;
    private Set<ServiceDescriptor> servicesLocations;
    private Set<ServiceDescriptor> servicesBlockedLocations;
    private RoundRobinPool pool;

    public InMemServiceLocator(String domain, Set<ServiceDescriptor> servicesLocations) {
        this.servicesBlockedLocations = new ConcurrentSet<>();
        this.domain = domain;
        this.servicesLocations = servicesLocations;
        this.pool = new RoundRobinPool();
        //update keys in pool - if absent
        this.pool.addServices(Collections.unmodifiableSet(servicesLocations));
    }

    @Override
    public Optional<ServiceDescriptor> getService(String uri, String version) {
        System.out.println("******* InMemServiceLocator :: *****************");
        Optional<String> serviceName = FilterUtils.extractService(uri);
        if (!serviceName.isPresent()) {
            System.out.println("InMemServiceLocator:: return empty");
            return Optional.empty();
        }

        //reload pool if services where removed/added
        if (servicesLocations.size() != pool.size()) {
            pool.updateService(servicesLocations);
        }
        ServiceVersion serviceVersion = new ServiceVersion(serviceName.get(), version);

        if (!servicesBlockedLocations.stream().anyMatch(descriptor -> descriptor.getServiceVersion().equals(serviceVersion))) {
            //get next (circular loop) round robin
            Optional<ServiceDescriptor> serviceDescriptor = pool.get(serviceVersion);
            System.out.println("InMemServiceLocator::result" + serviceDescriptor);
            return serviceDescriptor;
        }

        System.out.println("InMemServiceLocator::servicesBlockedLocations");
        servicesBlockedLocations.stream().forEach(System.out::println);
        Optional<ServiceDescriptor> descriptor = pool.get(serviceVersion, servicesBlockedLocations);
        System.out.println("InMemServiceLocator::result" + descriptor);
        return descriptor;
    }

    public String getDomain() {
        return domain;
    }

    public Collection<ServiceDescriptor> getAllProviders(ServiceVersion serviceVersion) {
        return Collections.unmodifiableCollection(pool.getAll(serviceVersion));
    }

    public void blockServiceProvider(ServiceDescriptor descriptor) {
        servicesBlockedLocations.add(descriptor);
    }

}

