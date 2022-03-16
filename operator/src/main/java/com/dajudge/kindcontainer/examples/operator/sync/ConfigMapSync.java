package com.dajudge.kindcontainer.examples.operator.sync;

import com.dajudge.kindcontainer.examples.operator.transport.Greeter;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.HashMap;

public class ConfigMapSync {
    private final KubernetesClient client;

    public ConfigMapSync(final KubernetesClient client) {
        this.client = client;
    }

    public ConfigMap sync(final Greeter greeter) {
        final var configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(greeter.getMetadata().getName())
                .withNamespace(greeter.getMetadata().getNamespace())
                .withOwnerReferences(new OwnerReferenceBuilder()
                        .withKind(greeter.getKind())
                        .withApiVersion(greeter.getApiVersion())
                        .withName(greeter.getMetadata().getName())
                        .withUid(greeter.getMetadata().getUid())
                        .build())
                .endMetadata()
                .withData(new HashMap<String, String>() {{
                    put("greeting.txt", greeter.getSpec().getGreeting());
                }})
                .build();
        return client.configMaps().createOrReplace(configMap);
    }
}
