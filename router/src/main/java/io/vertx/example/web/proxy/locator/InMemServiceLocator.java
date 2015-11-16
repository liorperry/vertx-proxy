package io.vertx.example.web.proxy.locator;

import com.google.common.collect.Sets;
import io.netty.util.internal.ConcurrentSet;
import io.vertx.example.web.proxy.filter.FilterUtils;

import java.util.*;

public class InMemServiceLocator implements ServiceLocator {
    private String domain;
    private Set<ServiceDescriptor> servicesBlockedLocations;
    private RoundRobinPool pool;
    private VerticalServiceRegistry registry;

    public InMemServiceLocator(String domain, VerticalServiceRegistry registry) {
        this.registry = registry;
        this.servicesBlockedLocations = new ConcurrentSet<>();
        this.domain = domain;
        this.pool = new RoundRobinPool();
        //update keys in pool - if absent
        this.pool.addServices(new HashSet<>(registry.getServices()));
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
        if (registry.size() != pool.size()) {
            pool.updateService(Sets.newHashSet(registry.getServices()));
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

    public Collection<ServiceDescriptor> getAllProviders() {
        return Collections.unmodifiableCollection(pool.getAll());
    }

    public void blockServiceProvider(ServiceDescriptor descriptor) {
        servicesBlockedLocations.add(descriptor);
    }

    public void unblockServiceProvider(ServiceDescriptor descriptor) {
        servicesBlockedLocations.remove(descriptor);
    }

}

