package com.ekuefler.supereventbus.shared;

import java.util.LinkedList;
import java.util.List;

public class ExceptionTest extends SuperEventBusTestCase {

  class TestOwner {
    private final List<Object> events = new LinkedList<Object>();

    @Subscribe
    void handleInteger(Integer event) {
      eventBus.post("string");
      eventBus.post('a');
      events.add(event);
    }

    @Subscribe
    void handleString(String event) {
      throw new ExpectedExceptionForTest();
    }

    @Subscribe
    void handleCharacter(Character event) {
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

  public void testShouldNotAllowPostingNulls() {
    try {
      eventBus.post(null);
      fail("Exception not thrown");
    } catch (NullPointerException expected) {}
  }

  public void testShouldContinueWhenHandlerThrowsException() {
    eventBus.post(123); // This will post a string causing an exception, then post a character
    assertEquals(listOf(123, 'a'), owner.events);
  }

  public void testShouldAlertExceptionHandlerWhenErrorsOccur() {
    final List<EventBusException> exceptions = new LinkedList<EventBusException>();
    eventBus.addExceptionHandler(new ExceptionHandler() {
      @Override
      public void handleException(EventBusException e) {
        exceptions.add(e);
      }
    });

    eventBus.post("string"); // Will throw an exception

    assertEquals(1, exceptions.size());
    assertEquals("string", exceptions.get(0).getEvent());
    assertTrue(exceptions.get(0).getCause() instanceof ExpectedExceptionForTest);
  }

  public void testShouldAlertExceptionHandlerWhenErrorsOccurInTransitiveHandlers() {
    final List<EventBusException> exceptions = new LinkedList<EventBusException>();
    eventBus.addExceptionHandler(new ExceptionHandler() {
      @Override
      public void handleException(EventBusException e) {
        exceptions.add(e);
      }
    });

    eventBus.post(123); // Will fire an event that will throw an exception

    assertEquals(1, exceptions.size());
    assertEquals("string", exceptions.get(0).getEvent()); // Was fired by the handler for Integer
    assertTrue(exceptions.get(0).getCause() instanceof ExpectedExceptionForTest);
  }

  public void testShouldIgnoreExceptionsInExceptionHandlers() {
    eventBus.addExceptionHandler(new ExceptionHandler() {
      @Override
      public void handleException(EventBusException e) {
        throw new ExpectedExceptionForTest();
      }
    });

    eventBus.post("string"); // Will throw an exception

    // Expect no exceptions
  }

  private static class ExpectedExceptionForTest extends RuntimeException {}
}
