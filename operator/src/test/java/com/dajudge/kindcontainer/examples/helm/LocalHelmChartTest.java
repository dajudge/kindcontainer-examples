package com.dajudge.kindcontainer.examples.helm;

import com.dajudge.kindcontainer.K3sContainer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.fabric8.kubernetes.client.Config.fromKubeconfig;
import static java.util.concurrent.TimeUnit.SECONDS;

public class LocalHelmChartTest {
    private static final int INGRESS_PORT = 30080;
    private static final String CHART_URL = "https://github.com/kubernetes/ingress-nginx/releases/download/helm-chart-4.0.18/ingress-nginx-4.0.18.tgz";

    @Rule
    public final K3sContainer<?> k8s = new K3sContainer<>()
            .withHelm3(helm -> {
                helm.copyFileToContainer(MountableFile.forHostPath(downloadChart()), "/tmp/chart.tgz");
                helm.install
                        .namespace("ingress-nginx")
                        .createNamespace(true)
                        .set("controller.service.type", "NodePort")
                        .set("controller.service.nodePorts.http", String.valueOf(INGRESS_PORT))
                        .run("ingress-nginx", "/tmp/chart.tgz");
            });

    private static Path downloadChart() {
        try {
            final byte[] chartBytes = RestAssured.get(new URL(CHART_URL)).asByteArray();
            final Path path = Files.createTempFile("kindcontainer-examples-", ".tgz");
            Files.write(path, chartBytes);
            return path;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void controller_becomes_ready() {
        try (final DefaultKubernetesClient client = new DefaultKubernetesClient(fromKubeconfig(k8s.getKubeconfig()))) {
            client.inNamespace("ingress-nginx").apps().deployments().withName("ingress-nginx-controller")
                    .waitUntilReady(120, SECONDS);
        }
    }
}
