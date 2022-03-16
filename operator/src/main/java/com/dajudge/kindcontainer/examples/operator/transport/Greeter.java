package com.dajudge.kindcontainer.examples.operator.transport;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("kindcontainer.dajudge.com")
public class Greeter extends CustomResource<GreeterSpec, GreeterStatus> implements Namespaced {

}
