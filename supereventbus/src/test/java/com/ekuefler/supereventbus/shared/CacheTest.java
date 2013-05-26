package com.ekuefler.supereventbus.shared;

public class CacheTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    int eventsHandled = 0;

    @Subscribe
    void handleString(String event) {
      eventsHandled++;
    }
  }

  public void testShouldInvokeHandlersRegisteredAfterFiring() {
    TestOwner owner1 = new TestOwner();
    TestOwner owner2 = new TestOwner();

    eventBus.register(owner1, TestOwner.MyRegistration.class);
    eventBus.post("string");
    eventBus.register(owner2, TestOwner.MyRegistration.class);
    eventBus.post("string");

    assertEquals(2, owner1.eventsHandled);
    assertEquals(1, owner2.eventsHandled);
  }

  public void testShouldBeAbleToReRegisterHandlersAfterEventsAreFired() {
    TestOwner owner = new TestOwner();

    eventBus.register(owner, TestOwner.MyRegistration.class);
    eventBus.post("string");
    eventBus.unregister(owner);
    eventBus.register(owner, TestOwner.MyRegistration.class);
    eventBus.post("string");

    assertEquals(2, owner.eventsHandled);
  }

  public void testShouldBeAbleToReRegisterHandlersBeforeEventsAreFired() {
    TestOwner owner1 = new TestOwner();
    TestOwner owner2 = new TestOwner();
    eventBus.register(owner1, TestOwner.MyRegistration.class);
    eventBus.post("string");

    eventBus.register(owner2, TestOwner.MyRegistration.class);
    eventBus.unregister(owner2);
    eventBus.register(owner2, TestOwner.MyRegistration.class);
    eventBus.post("string");

    assertEquals(2, owner1.eventsHandled);
    assertEquals(1, owner2.eventsHandled);
  }
}
