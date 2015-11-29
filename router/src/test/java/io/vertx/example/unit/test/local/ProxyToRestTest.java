package io.vertx.example.unit.test.local;

import com.codahale.metrics.health.HealthCheck;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.unit.test.integration.SimpleREST;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.example.web.proxy.repository.LocalCacheKeysRepository;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.vertx.example.web.proxy.filter.Filter.FilterBuilder.filterBuilder;

@RunWith(VertxUnitRunner.class)
public class ProxyToRestTest {

    public static final String LOCALHOST = "localhost";
    //SERVICES
    public static final String SERVICE_A_OPEN = "serviceA";
    public static final String SERVICE_B_BLOCKED = "serviceB";
    //PRODUCTS
    public static final String PROD3568_BLOCKED = "prod3568";
    public static final String PROD7340_OPED = "prod7340";
    public static final String PROD8643_OPEN = "prod8643";

    private static Vertx vertx;

    static DeploymentOptions restOptions = VertxInitUtils.initDeploymentOptions();
    static DeploymentOptions proxyOptions = VertxInitUtils.initDeploymentOptions(false);

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx(VertxInitUtils.initOptions());

        vertx.deployVerticle(new SimpleREST((result, domain, descriptor) -> HealthCheck.Result.healthy(),Publisher.EMPTY, Publisher.EMPTY, new VerticalServiceRegistry()),
                restOptions,
                context.asyncAssertSuccess());


        Set<ServiceDescriptor> services = new LinkedHashSet<>();
        services.add(ServiceDescriptor.create(SERVICE_A_OPEN, VertxInitUtils.getPort(restOptions)));
        VerticalServiceRegistry registry = new VerticalServiceRegistry(services);

        //keys & routes repository
        LocalCacheKeysRepository repository = new LocalCacheKeysRepository();
        repository.addService(SERVICE_A_OPEN, true);
        repository.addService(SERVICE_B_BLOCKED, false);
        repository.addProduct(PROD3568_BLOCKED, false);

        vertx.deployVerticle(new ProxyServer(
                        filterBuilder(repository)
                                .add(new ServiceFilter())
                                .add(new ProductFilter())
                                .build(),
                        (result, domain, descriptor) -> HealthCheck.Result.healthy(),
                        Publisher.EMPTY,
                        new VerticalServiceRegistry(),
                        new InMemServiceLocator(ProxyServer.PROXY, registry)
                ),
                proxyOptions,
                context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testProxyAlive(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        // Send a request and get a response
        int port = VertxInitUtils.getPort(restOptions);
        client.getNow(port, LOCALHOST, "/serviceA", resp -> {
            resp.bodyHandler(body -> {
                System.out.println("/serviceA" + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains("prod7340"));
                context.assertTrue(body.toString().contains("prod3568"));
                context.assertTrue(body.toString().contains("prod8643"));
//                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testServiceA(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        // Send a request and get a response
        String requestURI = "/" + SERVICE_A_OPEN;
        int port = VertxInitUtils.getPort(proxyOptions);

        client.request(HttpMethod.GET, port, LOCALHOST, requestURI).handler(resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(PROD3568_BLOCKED));
                context.assertTrue(body.toString().contains(PROD8643_OPEN));
                context.assertTrue(body.toString().contains(PROD8643_OPEN));
                async.complete();
            });
        }).putHeader("version", "1.0").end();
    }

    @Test
    public void testServiceAProd3568(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        String requestURI = "/" + SERVICE_A_OPEN + "/" + PROD3568_BLOCKED;
        int port = VertxInitUtils.getPort(proxyOptions);

        client.getNow(port, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(resp.statusCode() == HttpResponseStatus.FORBIDDEN.code());
                async.complete();
            });
        });
    }

    @Test
    public void testServiceB(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        String requestURI = "/" + SERVICE_B_BLOCKED;
        int port = VertxInitUtils.getPort(proxyOptions);

        client.getNow(port, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(resp.statusCode() == HttpResponseStatus.FORBIDDEN.code());
                async.complete();
            });
        });
    }

    @Test
    public void testServiceAProd7340(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        String requestURI = "/" + SERVICE_A_OPEN + "/" + PROD7340_OPED;
        int port = VertxInitUtils.getPort(proxyOptions);

        client.getNow(port, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(PROD7340_OPED));
                async.complete();
            });
        });
    }
}
