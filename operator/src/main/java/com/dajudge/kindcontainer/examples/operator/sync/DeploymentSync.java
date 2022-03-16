package com.dajudge.kindcontainer.examples.operator.sync;

import com.dajudge.kindcontainer.examples.operator.transport.Greeter;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.HashMap;

import static java.util.Collections.singletonList;

public class DeploymentSync {
    private final KubernetesClient client;

    public DeploymentSync(final KubernetesClient client) {
        this.client = client;
    }

    public Deployment sync(final Greeter resource, final ConfigMap configMap) {
        final var labels = new HashMap<String, String>() {{
            put("kindcontainer.dajudge.com/greeting", resource.getMetadata().getName());
        }};
        final var deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(resource.getMetadata().getName())
                .withNamespace(resource.getMetadata().getNamespace())
                .withOwnerReferences(new OwnerReferenceBuilder()
                        .withKind(resource.getKind())
                        .withApiVersion(resource.getApiVersion())
                        .withName(resource.getMetadata().getName())
                        .withUid(resource.getMetadata().getUid())
                        .build())
                .endMetadata()
                .withSpec(new DeploymentSpecBuilder()
                        .withNewSelector()
                        .withMatchLabels(labels)
                        .endSelector()
                        .withReplicas(1)
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withMetadata(new ObjectMetaBuilder()
                                        .withLabels(labels)
                                        .build())
                                .withSpec(new PodSpecBuilder()
                                        .withVolumes(new VolumeBuilder()
                                                .withName("static-resources")
                                                .withConfigMap(new ConfigMapVolumeSourceBuilder()
                                                        .withName(configMap.getMetadata().getName())
                                                        .build())
                                                .build())
                                        .withContainers(new ContainerBuilder()
                                                .withName("nginx")
                                                .withImage("nginx")
                                                .withImagePullPolicy("IfNotPresent")
                                                .withPorts(singletonList(new ContainerPortBuilder()
                                                        .withName("http")
                                                        .withContainerPort(80)
                                                        .build()))
                                                .withVolumeMounts(new VolumeMountBuilder()
                                                        .withName("static-resources")
                                                        .withMountPath("/usr/share/nginx/html")
                                                        .build())
                                                .withReadinessProbe(new ProbeBuilder()
                                                        .withPeriodSeconds(1)
                                                        .withTimeoutSeconds(1)
                                                        .withFailureThreshold(5)
                                                        .withSuccessThreshold(1)
                                                        .withHttpGet(new HTTPGetActionBuilder()
                                                                .withPort(new IntOrString(80))
                                                                .withPath("/greeting.txt")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
        return client.apps().deployments().createOrReplace(deployment);
    }
}
