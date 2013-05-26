package com.ekuefler.supereventbus.shared;

import java.util.List;

public class BasicTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private String string;
    private int integer;
    private String[] stringArray;
    private List<String> listOfString;

    @Subscribe
    void handleString(String event) {
      string = event;
    }

    @Subscribe
    void handleInteger(int event) {
      integer = event;
    }

    @Subscribe
    void handleStringArray(String[] event) {
      stringArray = event;
    }

    @Subscribe
    void handleListOfString(List<String> event) {
      listOfString = event;
    }
  }

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, TestOwner.MyRegistration.class);
  }

  public void testShouldDispatchObjects() throws Exception {
    eventBus.post("hello world");
    assertEquals("hello world", owner.string);
  }

  public void testShouldDispatchPrimitives() throws Exception {
    eventBus.post(123);
    assertEquals(123, owner.integer);
  }

  public void testShouldDispatchBoxedPrimitives() throws Exception {
    eventBus.post(new Integer(123));
    assertEquals(123, owner.integer);
  }

  public void testShouldDispatchArrays() throws Exception {
    eventBus.post(new String[] {"hello", "world"});

    assertEquals(2, owner.stringArray.length);
    assertEquals("hello", owner.stringArray[0]);
    assertEquals("world", owner.stringArray[1]);
  }

  public void testShouldDispatchGenericTypes() throws Exception {
    eventBus.post(listOf("hello", "world"));
    assertEquals(listOf("hello", "world"), owner.listOfString);
  }
}
