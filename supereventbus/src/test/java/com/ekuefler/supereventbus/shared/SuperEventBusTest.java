package com.ekuefler.supereventbus.shared;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.junit.client.GWTTestCase;

public class SuperEventBusTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.ekuefler.supereventbus.SuperEventBus";
  }

  public void testTest() throws Exception {
    EventBus eventBus = new EventBus();
    TestOwner owner = new TestOwner(eventBus);
    eventBus.register(owner, TestEventRegistration.class);
    eventBus.post("hello there");
    assertEquals("hello there", owner.string);
    assertEquals(123, (int) owner.integer);
    assertEquals(listOf("hello there", 123), owner.eventsInOrder);
  }

  interface TestEventRegistration extends EventRegistration<TestOwner> {}

  class TestOwner {
    private final List<Object> eventsInOrder = new LinkedList<Object>();
    private final EventBus eventBus;

    private String string;
    private Integer integer;

    TestOwner(EventBus eventBus) {
      this.eventBus = eventBus;
    }

    @Subscribe
    void handleString(String event) {
      eventBus.post(123);
      eventsInOrder.add(event);
      string = event;
    }

    @Subscribe
    void handleInteger(Integer event) {
      eventsInOrder.add(event);
      integer = event;
    }
  }

  private List<Object> listOf(Object... objects) {
    LinkedList<Object> list = new LinkedList<Object>();
    for (Object object : objects) {
      list.add(object);
    }
    return list;
  }
}
