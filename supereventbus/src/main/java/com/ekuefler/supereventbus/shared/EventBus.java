package com.ekuefler.supereventbus.shared;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.gwt.core.shared.GWT;

public class EventBus {

  private final List<EventHandler<?>> handlers = new LinkedList<EventHandler<?>>();
  private final List<ExceptionHandler> exceptionHandlers = new LinkedList<ExceptionHandler>();

  private final Queue<EventWithHandler<?>> eventsToDispatch = new LinkedList<EventWithHandler<?>>();
  private boolean isDispatching = false;

  public <T> void post(T event) {
    if (event == null) {
      throw new NullPointerException();
    }

    for (EventHandler<?> wildcardHandler : handlers) {
      @SuppressWarnings("unchecked")
      EventHandler<T> handler = (EventHandler<T>) wildcardHandler;
      eventsToDispatch.add(new EventWithHandler<T>(event, handler));
    }

    if (!isDispatching) {
      dispatchQueuedEvents();
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void dispatchQueuedEvents() {
    isDispatching = true;
    EventWithHandler<T> eventWithHandler;
    while ((eventWithHandler = (EventWithHandler<T>) eventsToDispatch.poll()) != null) {
      EventHandler<T> handler = eventWithHandler.handler;
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

  public <T> void register(T object, Class<? extends EventRegistration<T>> registrationClass) {
    EventRegistration<T> registration = GWT.create(registrationClass);
    EventHandler<T> handler = new EventHandler<T>();
    handler.owner = object;
    handler.registration = registration;
    handlers.add(handler);
  }

  public void unregister(Object object) {}

  public void addExceptionHandler(ExceptionHandler exceptionHandler) {
    exceptionHandlers.add(exceptionHandler);
  }

  private class EventHandler<T> {
    T owner;
    EventRegistration<T> registration;
  }

  private static class EventWithHandler<T> {
    final Object event;
    final EventHandler<T> handler;

    public EventWithHandler(Object event, EventHandler<T> handler) {
      this.event = event;
      this.handler = handler;
    }
  }
}
