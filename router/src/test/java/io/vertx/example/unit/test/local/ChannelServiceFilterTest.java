package io.vertx.example.unit.test.local;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.filter.ChannelFilter;
import io.vertx.example.web.proxy.repository.KeysRepository;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class ChannelServiceFilterTest {

    private Set<String> services;

    @Before
    public void setUp() throws Exception {
        services = new HashSet<>();
        services.add("service1");
        services.add("service2");
    }

    @Test
    public void testFilterTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        when(request.getHeader(eq("channel"))).thenReturn("channel.internet");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getChannelServices("channel.internet")).thenReturn(services);
        when(repository.getChannelService(anyString(), eq("channel.internet"))).thenReturn(Optional.of(Boolean.TRUE));

        ChannelFilter filter = new ChannelFilter();
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

        ChannelFilter filter = new ChannelFilter();
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

        ChannelFilter filter = new ChannelFilter();
        boolean response = filter.filter(request, repository);
        assertEquals(response,false);
    }
}
