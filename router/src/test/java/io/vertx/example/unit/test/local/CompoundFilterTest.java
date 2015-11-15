package io.vertx.example.unit.test.local;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.example.web.proxy.filter.Filter;
import io.vertx.example.web.proxy.filter.ProductFilter;
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
public class CompoundFilterTest {

    @Test
    public void testFilterServiceTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.TRUE));
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.TRUE));

        ServiceFilter serviceFilter = new ServiceFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(serviceFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, true);
    }

    @Test
    public void testFilterProductTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.TRUE));
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.TRUE));

        ProductFilter productFilter = new ProductFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(productFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, true);
    }

    @Test
    public void testFilterServiceAndProductTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.TRUE));
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.TRUE));

        ProductFilter productFilter = new ProductFilter();
        ServiceFilter serviceFilter = new ServiceFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(serviceFilter).add(productFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, true);
    }

    @Test
    public void testFilterServiceTrueAndProductFalse() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.FALSE));
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.TRUE));

        ProductFilter productFilter = new ProductFilter();
        ServiceFilter serviceFilter = new ServiceFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(serviceFilter).add(productFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, false);
    }

    @Test
    public void testFilterServiceFalseAndProductTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.TRUE));
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.FALSE));

        ProductFilter productFilter = new ProductFilter();
        ServiceFilter serviceFilter = new ServiceFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(serviceFilter).add(productFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, false);
    }

    @Test
    public void testFilterServiceTrueAndProductVoid() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.empty());
        when(repository.getService(anyString())).thenReturn(Optional.of(Boolean.TRUE));

        ProductFilter productFilter = new ProductFilter();
        ServiceFilter serviceFilter = new ServiceFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(serviceFilter).add(productFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, true);
    }

    @Test
    public void testFilterServiceVoidAndProductTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.TRUE));
        when(repository.getService(anyString())).thenReturn(Optional.empty());

        ProductFilter productFilter = new ProductFilter();
        ServiceFilter serviceFilter = new ServiceFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(serviceFilter).add(productFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, false);
    }

    @Test
    public void testFilterDefaultVoid() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getService(anyString())).thenReturn(Optional.empty());
        when(repository.getProduct(anyString())).thenReturn(Optional.empty());

        ProductFilter productFilter = new ProductFilter();
        ServiceFilter serviceFilter = new ServiceFilter();
        Filter filter = Filter.FilterBuilder.filterBuilder(repository).add(serviceFilter).add(productFilter).build();

        boolean response = filter.filter(request);
        assertEquals(response, false);
    }
}
