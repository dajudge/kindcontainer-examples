package com.dajudge.kindcontainer.examples.operator.reconciler;

import com.dajudge.kindcontainer.examples.operator.sync.ConfigMapSync;
import com.dajudge.kindcontainer.examples.operator.sync.DeploymentSync;
import com.dajudge.kindcontainer.examples.operator.sync.IngressSync;
import com.dajudge.kindcontainer.examples.operator.sync.ServiceSync;
import com.dajudge.kindcontainer.examples.operator.transport.Greeter;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.Phaser;

import static io.fabric8.kubernetes.client.Watcher.Action.ADDED;
import static io.fabric8.kubernetes.client.Watcher.Action.MODIFIED;

@Slf4j
public class GreetingController implements AutoCloseable {
    private final Watch watch;
    private final Phaser phaser = new Phaser(1);
    private final KubernetesClient client;

    public GreetingController(final KubernetesClient client) {
        this.client = client;
        watch = client.resources(Greeter.class).watch(new Watcher<>() {
            @Override
            public void eventReceived(final Action action, final Greeter resource) {
                log.info("{} {} {}", action, resource.getFullResourceName(), resource.getMetadata().getName());
                try {
                    phaser.register();
                    reconcile(action, resource);
                } finally {
                    phaser.arriveAndDeregister();
                }
            }

            @Override
            public void onClose(final WatcherException cause) {
                log.error("Watcher closed abnormally.", cause);
                phaser.arriveAndAwaitAdvance();
            }

            @Override
            public void onClose() {
                log.info("Watcher closed gracefully.");
                phaser.arriveAndAwaitAdvance();
            }
        });
    }

    public void reconcile(final Watcher.Action action, final Greeter resource) {
        if (Arrays.asList(ADDED, MODIFIED).contains(action)) {
            final var configMap = new ConfigMapSync(client).sync(resource);
            final var deployment = new DeploymentSync(client).sync(resource, configMap);
            final var service = new ServiceSync(client).sync(resource, deployment);
            final var ingress = new IngressSync(client).sync(resource, service);
        }
    }

    @Override
    public void close() {
        watch.close();
    }
}
