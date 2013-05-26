package com.ekuefler.supereventbus.shared;

import java.util.LinkedList;
import java.util.List;

public class OrderingTest extends SuperEventBusTestCase {

  class TestOwner {
    final List<Object> events = new LinkedList<Object>();

    @Subscribe
    void handleInteger(Integer event) {
      eventBus.post("string1");
      events.add(event);
      eventBus.post("string2");
    }

    @Subscribe
    void handleString(String event) {
      eventBus.post('a');
      events.add(event);
      eventBus.post('b');
    }

    @Subscribe
    void handleChar(Character event) {
      events.add(event);
    }
  }

  interface MyRegistration extends EventRegistration<TestOwner> {}

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, MyRegistration.class);
  }

  public void testEventOrdering() {
    eventBus.post(123);

    assertEquals(listOf(123, "string1", "string2", 'a', 'b', 'a', 'b'), owner.events);
  }
}
