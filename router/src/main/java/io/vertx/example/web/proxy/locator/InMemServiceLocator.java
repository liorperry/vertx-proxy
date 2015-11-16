package io.vertx.example.web.proxy.locator;

import com.google.common.collect.Sets;
import io.vertx.example.web.proxy.filter.FilterUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemServiceLocator implements ServiceLocator {
    private String domain;
    private Map<String,ServiceDescriptor> servicesBlockedLocations;
    private RoundRobinPool pool;
    private VerticalServiceRegistry registry;

    public InMemServiceLocator(String domain, VerticalServiceRegistry registry) {
        this.registry = registry;
        this.servicesBlockedLocations = new ConcurrentHashMap<>();
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

        if (!servicesBlockedLocations.values().stream().anyMatch(descriptor -> descriptor.getServiceVersion().equals(serviceVersion))) {
            //get next (circular loop) round robin
            Optional<ServiceDescriptor> serviceDescriptor = pool.get(serviceVersion);
            System.out.println("InMemServiceLocator::result" + serviceDescriptor);
            return serviceDescriptor;
        }

        System.out.println("InMemServiceLocator::servicesBlockedLocations");
        servicesBlockedLocations.values().stream().forEach(System.out::println);
        Optional<ServiceDescriptor> descriptor = pool.get(serviceVersion, Sets.newHashSet(servicesBlockedLocations.values()));
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

    public Optional<ServiceDescriptor> blockServiceProvider(String id) {
        Optional<ServiceDescriptor> provider = pool.getProvider(id);
        if(!provider.isPresent())
            return provider;

        return blockServiceProvider(provider.get());
    }
    public Optional<ServiceDescriptor> blockServiceProvider(ServiceDescriptor descriptor) {
        return Optional.ofNullable(servicesBlockedLocations.put(descriptor.getKey(), descriptor));
    }

    public Optional<ServiceDescriptor> unblockServiceProvider(ServiceDescriptor descriptor) {
        return Optional.ofNullable(servicesBlockedLocations.remove(descriptor.getKey()));
    }

    public Optional<ServiceDescriptor> unblockServiceProvider(String id) {
        return Optional.ofNullable(servicesBlockedLocations.remove(id));
    }

}

