package com.dajudge.kindcontainer.examples.minimal;

import com.dajudge.kindcontainer.K3sContainer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static io.fabric8.kubernetes.client.Config.fromKubeconfig;

/**
 * This class documents the minimal useful k3s test case.
 */
public class MinimalK3sTest {

    @ClassRule
    public static K3sContainer<?> K8S = new K3sContainer<>();

    private DefaultKubernetesClient client;

    @Before
    public void setup() {
        client = new DefaultKubernetesClient(fromKubeconfig(K8S.getKubeconfig()));
    }

    @After
    public void teardown() {
        client.close();
    }

    @Test
    public void my_kindcontainer_test() {
        // Test something with the client
    }
}
