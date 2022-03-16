package com.dajudge.kindcontainer.examples.evolution;

import com.dajudge.kindcontainer.ApiServerContainer;
import com.dajudge.kindcontainer.examples.operator.reconciler.GreetingController;
import com.dajudge.kindcontainer.exception.ExecutionException;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import org.junit.*;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static io.fabric8.kubernetes.client.Config.fromKubeconfig;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;

public class Step1Test {
    // GIVEN: an ApiServer with our CRD
    @ClassRule
    public static final ApiServerContainer<?> K8S = new ApiServerContainer<>()
            .withKubectl(kubectl -> kubectl
                    .apply
                    .fileFromClasspath("manifests/crd.yaml")
                    .run());

    private NamespacedKubernetesClient client;
    private GreetingController controller;

    @Before
    public void setup() throws IOException, ExecutionException, InterruptedException {
        // GIVEN: a new namespace per test run
        final String namespace = UUID.randomUUID().toString();

        // GIVEN: a client
        client = new DefaultKubernetesClient(fromKubeconfig(K8S.getKubeconfig()))
                .inNamespace(namespace);
        client.namespaces().create(new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .build());

        // GIVEN: a controller instance
        controller = new GreetingController(client);

        // WHEN: A greeter CR is created
        K8S.kubectl().apply
                .fileFromClasspath("test-manifests/greeter.yaml", bytes -> new String(bytes, UTF_8)
                        .replace("default", namespace)
                        .getBytes(UTF_8))
                .run();
    }

    @After
    public void teardown() {
        controller.close();
        client.close();
    }

    @Test
    public void creates_deployment() {
        // THEN: a deployment should be created
        await().timeout(ofSeconds(5))
                .until(() -> client.apps().deployments().withName("my-greeter").get(), Objects::nonNull);
    }

    @Test
    public void creates_service() {
        // THEN: a service should be created
        await().timeout(ofSeconds(5))
                .until(() -> client.services().withName("my-greeter").get(), Objects::nonNull);
    }
}
