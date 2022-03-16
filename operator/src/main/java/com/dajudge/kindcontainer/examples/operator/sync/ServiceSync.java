package com.dajudge.kindcontainer.examples.operator.sync;

import com.dajudge.kindcontainer.examples.operator.transport.Greeter;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Map;

public class ServiceSync {
    private final KubernetesClient client;

    public ServiceSync(final KubernetesClient client) {
        this.client = client;
    }

    public Service sync(final Greeter greeter, final Deployment deployment) {
        final var selector = deployment.getSpec().getSelector().getMatchLabels();
        final var container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        final var port = container.getPorts().get(0);
        final var service = new ServiceBuilder()
                .withNewMetadata()
                .withName(greeter.getMetadata().getName())
                .withNamespace(greeter.getMetadata().getNamespace())
                .withOwnerReferences(new OwnerReferenceBuilder()
                        .withApiVersion(greeter.getApiVersion())
                        .withKind(greeter.getKind())
                        .withName(greeter.getMetadata().getName())
                        .withUid(greeter.getMetadata().getUid())
                        .build())
                .endMetadata()
                .withSpec(new ServiceSpecBuilder()
                        .withType("ClusterIP")
                        .withSelector(selector)
                        .withPorts(new ServicePortBuilder()
                                .withName(port.getName())
                                .withNewTargetPort(port.getName())
                                .withPort(80)
                                .build())
                        .build())
                .build();
        return client.services().createOrReplace(service);
    }
}
