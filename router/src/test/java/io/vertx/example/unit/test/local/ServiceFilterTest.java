package io.vertx.example.unit.test.local;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.repository.KeysRepository;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class ServiceFilterTest {

    @Test
    public void testFilterTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.TRUE));

        ServiceFilter filter = new ServiceFilter();
        boolean response = filter.filter(request, repository);
        assertEquals(response,true);
    }

    @Test
    public void testFilterFalse() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.FALSE));

        ServiceFilter filter = new ServiceFilter();
        boolean response = filter.filter(request, repository);
        assertEquals(response,false);
    }
    @Test
    public void testFilterDefaultFalse() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getService(anyString())).thenReturn(Optional.empty());

        ServiceFilter filter = new ServiceFilter();
        boolean response = filter.filter(request, repository);
        assertEquals(response,false);
    }
}
