package io.vertx.example.unit.test.local;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.unit.test.integration.SimpleREST;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.healthcheck.InMemHealthReporter;
import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.ServiceVersion;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.example.web.proxy.repository.KeysRepository;
import io.vertx.example.web.proxy.repository.LocalCacheKeysRepository;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.HashSet;
import java.util.Set;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;
import static io.vertx.example.web.proxy.filter.Filter.FilterBuilder.filterBuilder;
import static junit.framework.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class MultiProxyToMultiRestWithServiceBlockingTest {

    public static final int PROXY_PORT = 8080;

    private static final int REST1_PORT = 8082;
    private static final int REST2_PORT = 8083;
    private static final int REST3_PORT = 8084;

    public static final String LOCALHOST = "localhost";

    //SERVICES
    private static final String WHO_AM_I = "whoAmI";


    private static Set<String> serviceProvidersAddress;
    private static Vertx vertx;
    private static KeysRepository keysRepository;
    private static InMemServiceLocator locator;

    private static String hostAddress;

    //realtime set of available services
    private static VerticalServiceRegistry registry = new VerticalServiceRegistry();



    @BeforeClass
    public static void setUp(TestContext context) throws IOException, InterruptedException {
        hostAddress = Inet4Address.getLocalHost().getHostAddress();

        //start verticals
        vertx = Vertx.vertx(VertxInitUtils.initOptions());

        serviceProvidersAddress = new HashSet<>();
        //deploy rest server
        vertx.deployVerticle(new SimpleREST(new InMemHealthReporter(registry),Publisher.EMPTY, Publisher.EMPTY,registry ),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST1_PORT)),
                context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleREST(new InMemHealthReporter(registry),Publisher.EMPTY, Publisher.EMPTY,registry ),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST2_PORT)),
                context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleREST(new InMemHealthReporter(registry),Publisher.EMPTY, Publisher.EMPTY,registry ),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST3_PORT)),
                context.asyncAssertSuccess());


        //keys & routes repository
        keysRepository= new LocalCacheKeysRepository();
        keysRepository.addService(WHO_AM_I, true);

        //proxy vertical deployment
        locator = new InMemServiceLocator(ProxyServer.PROXY, registry);
        vertx.deployVerticle(new ProxyServer(
                        filterBuilder(keysRepository)
                                .add(new ServiceFilter())
                                .add(new ProductFilter())
                                .build(),
                        (result, domain, descriptor) -> HealthCheck.Result.healthy(),
                        Publisher.EMPTY,
                        new VerticalServiceRegistry(),
                        locator),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put(HTTP_PORT, PROXY_PORT)
                        .put(ENABLE_METRICS_PUBLISH, false)),
                context.asyncAssertSuccess());
    }

    @Test
    public void testServiceWhoAmIFirstAndBlockRest3(TestContext context) throws InterruptedException {
        Async async = context.async();
        // Send a request and get a response
        final String requestURI = "/" + WHO_AM_I;
        HttpClient httpClient = vertx.createHttpClient();

        //first request
        httpClient.getNow(PROXY_PORT, LOCALHOST, requestURI, resp1 -> {
            resp1.bodyHandler(body1 -> {
                JsonObject entries1 = new JsonObject(body1.toString());
                System.out.println(requestURI + ":" + entries1.encodePrettily());
                serviceProvidersAddress.add(WHO_AM_I + ":" + entries1.getValue("/" + WHO_AM_I));
                //block REST 3 service (assuming the first one returning wasnt REST3 )
                locator.blockServiceProvider(ServiceDescriptor.create(new ServiceVersion(WHO_AM_I,"1"), hostAddress , REST3_PORT));
                async.complete();
            });
        });

    }

    @Test
    public void testServiceWhoAmISecond(TestContext context) throws InterruptedException {
        Async async = context.async();
        // Send a request and get a response
        final String requestURI = "/" + WHO_AM_I;
        HttpClient httpClient = vertx.createHttpClient();

        //first request
        httpClient.getNow(PROXY_PORT, LOCALHOST, requestURI, resp1 -> {
            resp1.bodyHandler(body1 -> {
                JsonObject entries1 = new JsonObject(body1.toString());
                System.out.println(requestURI + ":" + entries1.encodePrettily());
                serviceProvidersAddress.add(WHO_AM_I +":"+entries1.getValue("/" + WHO_AM_I));
                async.complete();
            });
        });
    }

    @Test
    public void testServiceWhoAmIThird(TestContext context) throws InterruptedException {
        Async async = context.async();
        // Send a request and get a response
        final String requestURI = "/" + WHO_AM_I;
        HttpClient httpClient = vertx.createHttpClient();

        //first request
        httpClient.getNow(PROXY_PORT, LOCALHOST, requestURI, resp1 -> {
            resp1.bodyHandler(body1 -> {
                JsonObject entries1 = new JsonObject(body1.toString());
                System.out.println(requestURI + ":" + entries1.encodePrettily());
                serviceProvidersAddress.add(WHO_AM_I +":"+entries1.getValue("/" + WHO_AM_I));
                async.complete();
            });
        });
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("******** serviceProvidersAddress **********");
        serviceProvidersAddress.stream().forEach(System.out::println);
        assertEquals(serviceProvidersAddress.size(), 3);
        vertx.close();
    }
}
