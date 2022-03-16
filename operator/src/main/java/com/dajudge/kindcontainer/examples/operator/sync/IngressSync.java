package com.dajudge.kindcontainer.examples.operator.sync;

import com.dajudge.kindcontainer.examples.operator.transport.Greeter;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.networking.v1.*;
import io.fabric8.kubernetes.client.KubernetesClient;

public class IngressSync {
    private final KubernetesClient client;

    public IngressSync(final KubernetesClient client) {
        this.client = client;
    }


    public Ingress sync(final Greeter resource, final Service service) {
        final var port = service.getSpec().getPorts().get(0);
        final var ingress = new IngressBuilder()
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
                .withSpec(new IngressSpecBuilder()
                        .withIngressClassName("nginx")
                        .withRules(new IngressRuleBuilder()
                                .withHost(resource.getSpec().getHost())
                                .withHttp(new HTTPIngressRuleValueBuilder()
                                        .withPaths(new HTTPIngressPathBuilder()
                                                .withPath("/")
                                                .withPathType("Prefix")
                                                .withBackend(new IngressBackendBuilder()
                                                        .withService(new IngressServiceBackendBuilder()
                                                                .withName(service.getMetadata().getName())
                                                                .withPort(new ServiceBackendPortBuilder()
                                                                        .withName(port.getName())
                                                                        .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build())
                .build();
        return client.network().v1().ingresses().createOrReplace(ingress);
    }
}
