package io.vertx.example.unit.test.local;

import io.vertx.example.web.proxy.repository.LocalCacheKeysRepository;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class LocalCacheKeysRepositoryTest {
    private LocalCacheKeysRepository repository;

    @Test
    public void testCloseRepository() throws Exception {
        repository = new LocalCacheKeysRepository();
        repository.addService("service1", true);
        repository.addProduct("product", false);

        assertEquals(repository.getServices().size(), 1);
        assertEquals(repository.getProducts().size(), 1);

        repository.close(event -> {
        });

        assertEquals(repository.getServices().size(), 0);
        assertEquals(repository.getProducts().size(), 0);


    }

    public void testAddAndBlockService() throws Exception {
        repository = new LocalCacheKeysRepository();
        repository.addService("service1", true);
        assertEquals(repository.getService("/service1/product").get(), Boolean.TRUE);
        assertEquals(repository.getService("/service1/").get(), Boolean.TRUE);
        repository.blockService("service1");
        assertEquals(repository.getService("/service1/product").get(), Boolean.FALSE);
        assertEquals(repository.getService("/service1/").get(), Boolean.FALSE);
    }

    @Test
    public void testAddAndOpenService() throws Exception {
        repository = new LocalCacheKeysRepository();
        repository.addService("service1", false);
        assertEquals(repository.getService("/service1/product").get(), Boolean.FALSE);
        assertEquals(repository.getService("/service1/").get(), Boolean.FALSE);
        repository.openService("service1");
        assertEquals(repository.getService("/service1/product").get(), Boolean.TRUE);
        assertEquals(repository.getService("/service1/").get(), Boolean.TRUE);
    }

    @Test
    public void testAddAndBlockProduct() throws Exception {
        repository = new LocalCacheKeysRepository();
        repository.addProduct("product1", true);
        assertEquals(repository.getProduct("/service/product1").get(), Boolean.TRUE);
        repository.blockProduct("product1");
        assertEquals(repository.getProduct("/service/product1").get(), Boolean.FALSE);
    }

    @Test
    public void testAddAndOpenProduct() throws Exception {
        repository = new LocalCacheKeysRepository();
        repository.addProduct("product1", false);
        assertEquals(repository.getProduct("/service/product1").get(), Boolean.FALSE);
        repository.openProduct("product1");
        assertEquals(repository.getProduct("/service/product1").get(), Boolean.TRUE);
    }

    public void testAddService() throws Exception {
        repository = new LocalCacheKeysRepository();
        repository.addService("service1", true);
        repository.addService("service2", false);

        assertEquals(repository.getServices().size(), 2);

        assertEquals(repository.getService("/service1/product").get(), Boolean.TRUE);
        assertEquals(repository.getService("/service1/").get(), Boolean.TRUE);
        assertEquals(repository.getService("/service2/product").get(), Boolean.FALSE);
        assertEquals(repository.getService("/service2/").get(), Boolean.FALSE);
        assertEquals(repository.getService("/service3/").isPresent(),false);

    }

    @Test
    public void testAddProduct() throws Exception {
        repository = new LocalCacheKeysRepository();
        repository.addProduct("product1", true);
        repository.addProduct("product2", false);

        assertEquals(repository.getProducts().size(), 2);

        assertEquals(repository.getProduct("/service/product1").get(), Boolean.TRUE);
        assertEquals(repository.getProduct("/service/product2").get(), Boolean.FALSE);
        assertEquals(repository.getProduct("/service/product1").get(), Boolean.TRUE);
        assertEquals(repository.getProduct("/service/product2").get(), Boolean.FALSE);
        assertEquals(repository.getProduct("/service/product3").isPresent(),false);

    }

}
