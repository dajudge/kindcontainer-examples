Kindcontainer examples
-

This repository contains examples and showcase tests for [Kindcontainer](https://github.com/dajudge/kindcontainer).

# Topics
* [Greeting Operator](operator/src/main/java/com/dajudge/kindcontainer/examples/operator/reconciler/GreetingController.java)
  * Minimal
    * [Minimal ApiServer](operator/src/test/java/com/dajudge/kindcontainer/examples/minimal/MinimalApiServerTest.java)
    * [Minimal K3s](operator/src/test/java/com/dajudge/kindcontainer/examples/minimal/MinimalK3sTest.java)
    * [Minimal Kind](operator/src/test/java/com/dajudge/kindcontainer/examples/minimal/MinimalKindTest.java)
  * Evolution
    * [Step 1: ApiServer](operator/src/test/java/com/dajudge/kindcontainer/examples/evolution/Step1Test.java)
    * [Step 3: K3sContainer with Ingress](operator/src/test/java/com/dajudge/kindcontainer/examples/evolution/Step2Test.java)
    * [Step 4: K3sContainer with Ingress & RBAC](operator/src/test/java/com/dajudge/kindcontainer/examples/evolution/Step3Test.java)
