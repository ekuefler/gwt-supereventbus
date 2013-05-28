package com.ekuefler.supereventbus.shared;

import java.util.LinkedList;
import java.util.List;

import com.ekuefler.supereventbus.shared.priority.WithPriority;

public class PriorityTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private final List<Integer> handledPriorities = new LinkedList<Integer>();

    @Subscribe
    @WithPriority(-10)
    void priortyNeg10(String event) {
      handledPriorities.add(-10);
    }

    @Subscribe
    @WithPriority(10)
    void priorty10(String event) {
      handledPriorities.add(10);
    }

    @Subscribe
    @WithPriority(20)
    void priorty20(String event) {
      handledPriorities.add(20);
    }

    @Subscribe
    @WithPriority(-20)
    void priortyNeg20(String event) {
      handledPriorities.add(-20);
    }

    @Subscribe
    void priorty0(String event) {
      handledPriorities.add(0);
    }
  }

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, TestOwner.MyRegistration.class);
  }

  public void testShouldHandleInPriorityOrder() {
    eventBus.post("event");
    assertEquals(listOf(20, 10, 0, -10, -20), owner.handledPriorities);
  }
}
