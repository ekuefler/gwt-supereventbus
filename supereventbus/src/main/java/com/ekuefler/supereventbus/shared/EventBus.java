package com.ekuefler.supereventbus.shared;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.gwt.core.shared.GWT;

public class EventBus {

  private final List<EventHandler> handlers = new LinkedList<EventHandler>();
  private final List<ExceptionHandler> exceptionHandlers = new LinkedList<ExceptionHandler>();

  private final Queue<EventWithHandler> eventsToDispatch = new LinkedList<EventWithHandler>();
  private boolean isDispatching = false;

  public void post(Object event) {
    if (event == null) {
      throw new NullPointerException();
    }

    for (EventHandler handler : handlers) {
      eventsToDispatch.add(new EventWithHandler(event, handler));
    }
    if (!isDispatching) {
      dispatchQueuedEvents();
    }
  }

  private void dispatchQueuedEvents() {
    isDispatching = true;
    EventWithHandler eventWithHandler;
    while ((eventWithHandler = eventsToDispatch.poll()) != null) {
      EventHandler handler = eventWithHandler.handler;
      try {
        handler.registration.dispatch(handler.owner, eventWithHandler.event);
      } catch (Exception e) {
        for (ExceptionHandler exceptionHandler : exceptionHandlers) {
          try {
            exceptionHandler.handleException(e);
          } catch (Exception ex) {
            // Give up
          }
        }
      }
    }
    isDispatching = false;
  }

  public <T extends EventRegistration<?>> void register(Object object, Class<T> registrationClass) {
    EventRegistration<?> registration = GWT.create(registrationClass);
    EventHandler handler = new EventHandler();
    handler.owner = object;
    handler.registration = registration;
    handlers.add(handler);
  }

  public void unregister(Object object) {}

  public void addExceptionHandler(ExceptionHandler exceptionHandler) {
    exceptionHandlers.add(exceptionHandler);
  }

  private class EventHandler {
    Object owner;
    EventRegistration<?> registration;
  }

  private static class EventWithHandler {
    final Object event;
    final EventHandler handler;

    public EventWithHandler(Object event, EventHandler handler) {
      this.event = event;
      this.handler = handler;
    }
  }
}
