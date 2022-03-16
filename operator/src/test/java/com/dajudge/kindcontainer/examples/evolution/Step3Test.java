package com.dajudge.kindcontainer.examples.evolution;

import com.dajudge.kindcontainer.K3sContainer;
import com.dajudge.kindcontainer.examples.operator.reconciler.GreetingController;
import com.dajudge.kindcontainer.exception.ExecutionException;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static com.dajudge.kindcontainer.DeploymentAvailableWaitStrategy.deploymentIsAvailable;
import static io.fabric8.kubernetes.client.Config.fromKubeconfig;
import static io.restassured.RestAssured.get;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;

public class Step3Test {
    private static final int INGRESS_PORT = 30080;

    // GIVEN: an K3sContainer with our CRD and the operator's RBAC manifests
    @ClassRule
    public static final K3sContainer<?> K8S = new K3sContainer<>()
            .withKubectl(kubectl -> kubectl
                    .apply
                    .fileFromClasspath("manifests/crd.yaml")
                    .fileFromClasspath("manifests/rbac.yaml")
                    .run())
            .withExposedPorts(INGRESS_PORT)
            .withHelm3(helm -> {
                helm.repo.add.run("ingress-nginx", "https://kubernetes.github.io/ingress-nginx");
                helm.repo.update.run();
                helm.install
                        .namespace("ingress-nginx")
                        .createNamespace(true)
                        .set("controller.service.type", "NodePort")
                        .set("controller.service.nodePorts.http", String.valueOf(INGRESS_PORT))
                        .run("ingress-nginx", "ingress-nginx/ingress-nginx");
            })
            .waitingFor(deploymentIsAvailable("ingress-nginx", "ingress-nginx-controller"));

    private NamespacedKubernetesClient adminClient;
    private GreetingController controller;
    private String hostname;
    private NamespacedKubernetesClient rbacClient;

    @Before
    public void setup() throws IOException, ExecutionException, InterruptedException {
        // GIVEN: a new namespace per test run
        final String namespace = UUID.randomUUID().toString();

        // GIVEN: an admin client
        adminClient = new DefaultKubernetesClient(fromKubeconfig(K8S.getKubeconfig()))
                .inNamespace(namespace);
        adminClient.namespaces().create(new NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .build());

        // GIVEN: a limited client
        rbacClient = new DefaultKubernetesClient(fromKubeconfig(
                K8S.getServiceAccountKubeconfig("default", "greeter-operator")
        )).inNamespace(namespace);

        // GIVEN: a controller instance
        controller = new GreetingController(rbacClient);

        // WHEN: A greeter CR is created (with a per namespace hostname)
        final String ip = InetAddress.getByName(K8S.getHost()).getHostAddress();
        hostname = String.format("%s.%s.nip.io", namespace, ip);
        K8S.kubectl().apply
                .fileFromClasspath("test-manifests/greeter.yaml", bytes -> new String(bytes, UTF_8)
                        .replace("default", namespace)
                        .replace("hello.127.0.0.1.nip.io", hostname)
                        .getBytes(UTF_8))
                .run();
    }

    @After
    public void teardown() {
        controller.close();
        rbacClient.close();
        adminClient.close();
    }

    @Test
    public void creates_deployment() {
        // THEN: a deployment should be created
        await().timeout(ofSeconds(5))
                .until(() -> adminClient.apps().deployments().withName("my-greeter").get(), Objects::nonNull);
    }

    @Test
    public void creates_service() {
        // THEN: a service should be created
        await().timeout(ofSeconds(5))
                .until(() -> adminClient.services().withName("my-greeter").get(), Objects::nonNull);
    }

    @Test
    public void exposes_via_ingress() throws UnknownHostException {
        // THEN: the greeting should be available via HTTP
        // The node port will be mapped to a random port outside the container
        final var url = String.format("http://%s:%d/greeting.txt", hostname, K8S.getMappedPort(INGRESS_PORT));
        await().timeout(ofSeconds(120))
                .ignoreExceptions()
                .until(() -> get(new URL(url)).asString(), "Hello, world!"::equals);
    }
}
