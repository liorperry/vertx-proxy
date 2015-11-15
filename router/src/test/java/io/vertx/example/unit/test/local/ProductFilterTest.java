package io.vertx.example.unit.test.local;

import io.vertx.core.http.HttpServerRequest;
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
public class ProductFilterTest {

    @Test
    public void testFilterTrue() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.TRUE));

        ProductFilter filter = new ProductFilter();
        boolean response = filter.filter(request, repository);
        assertEquals(response,true);
    }

    @Test
    public void testFilterFalse() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.of(Boolean.FALSE));

        ProductFilter filter = new ProductFilter();
        boolean response = filter.filter(request, repository);
        assertEquals(response,false);
    }
    @Test
    public void testFilterDefaultFalse() throws Exception {
        HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        when(request.uri()).thenReturn("/service1/product1:#");
        KeysRepository repository = Mockito.mock(KeysRepository.class);
        when(repository.getProduct(anyString())).thenReturn(Optional.empty());

        ProductFilter filter = new ProductFilter();
        boolean response = filter.filter(request, repository);
        assertEquals(response,true);
    }
}
