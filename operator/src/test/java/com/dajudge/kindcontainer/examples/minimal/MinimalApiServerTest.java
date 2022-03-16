package com.dajudge.kindcontainer.examples.minimal;

import com.dajudge.kindcontainer.ApiServerContainer;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static io.fabric8.kubernetes.client.Config.fromKubeconfig;

/**
 * This class documents the minimal useful API server test case.
 */
public class MinimalApiServerTest {

    @ClassRule
    public static ApiServerContainer<?> K8S = new ApiServerContainer<>();

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
    public void my_apiserver_test() {
        // Test something with the client
    }
}
