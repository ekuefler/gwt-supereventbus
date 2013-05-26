package com.ekuefler.supereventbus.shared;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PolymorphismTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private int objectsHandled = 0;
    private int listsHandled = 0;
    private int sequentialListsHandled = 0;
    private int linkedListsHandled = 0;

    @Subscribe
    void handleObject(Object event) {
      objectsHandled++;
    }

    @Subscribe
    void handleInterface(List<?> event) {
      listsHandled++;
    }

    @Subscribe
    void handleAbstractClass(AbstractSequentialList<?> event) {
      sequentialListsHandled++;
    }

    @Subscribe
    void handleConcreteType(LinkedList<?> event) {
      linkedListsHandled++;
    }
  }

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, TestOwner.MyRegistration.class);
  }

  public void testShouldHandleObjects() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(4, owner.objectsHandled);
  }

  public void testShouldHandleInterfaces() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(2, owner.listsHandled);
  }

  public void testShouldHandleAbstractClasses() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(1, owner.sequentialListsHandled);
  }

  public void testShouldHandleLinkedLists() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(1, owner.linkedListsHandled);
  }
}
