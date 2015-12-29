package io.vertx.example.unit.test.local;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.example.web.proxy.filter.Feature.ProxyFeatureManagerProvider;
import io.vertx.example.web.proxy.filter.GenericScriptFilter;
import io.vertx.example.web.proxy.repository.KeysRepository;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.*;

import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.buildFeatureMetadata;
import static io.vertx.example.unit.test.local.filter.feature.FeatureUtilityTest.buildFeatureStatus;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class GenericScriptServiceFilterTest {

    private Set<String> services;

    private ProxyFeatureManagerProvider provider;

    private JsonArray featureArray;
    private JsonArray statusArray;

    @Before
    public void setUp() throws Exception {
        services = new HashSet<>();
        services.add("service1");
        services.add("service2");

        featureArray = new JsonArray();
        statusArray = new JsonArray();

        featureArray.add(buildFeatureMetadata("feature1", true, Optional.of(Arrays.asList("1", "2", "3"))));
        featureArray.add(buildFeatureMetadata("feature2", false, Optional.<List<String>>empty()));

        statusArray.add(buildFeatureStatus("feature1", "StrategyId", true, Optional.of(Arrays.asList("1", "2", "3")), Collections.emptyMap()));
        statusArray.add(buildFeatureStatus("feature2", "StrategyId", false, Optional.<List<String>>empty(), Collections.emptyMap()));
        provider = new ProxyFeatureManagerProvider(featureArray,statusArray);

    }

    @Test
    public void testFilterTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        when(request.getHeader(eq("channel"))).thenReturn("channel.internet");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getChannelServices("channel.internet")).thenReturn(services);
        when(repository.getChannelService(anyString(), eq("channel.internet"))).thenReturn(Optional.of(Boolean.TRUE));

        GenericScriptFilter filter = new GenericScriptFilter(provider.getFeatureManager());
        boolean response = filter.filter(request, repository);
        assertEquals(response,true);
    }

    @Test
    public void testFilterFalse() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service3/product1:#");
        when(request.getHeader(eq("channel"))).thenReturn("channel.internet");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getChannelServices("channel.internet")).thenReturn(services);
        when(repository.getChannelService(anyString(), eq("channel.internet"))).thenReturn(Optional.of(Boolean.FALSE));

        GenericScriptFilter filter = new GenericScriptFilter(provider.getFeatureManager());
        boolean response = filter.filter(request, repository);
        assertEquals(response,false);
    }
    @Test
    public void testFilterDefaultFalse() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        when(request.getHeader(eq("channel"))).thenReturn("channel.internet");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getChannelServices("channel.internet")).thenReturn(Collections.<String>emptySet());
        when(repository.getChannelService(anyString(), eq("channel.internet"))).thenReturn(Optional.<Boolean>empty());

        GenericScriptFilter filter = new GenericScriptFilter(provider.getFeatureManager());
        boolean response = filter.filter(request, repository);
        assertEquals(response,false);
    }
}
