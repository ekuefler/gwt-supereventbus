package com.ekuefler.supereventbus.shared;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.ekuefler.supereventbus.shared.impl.Method;
import com.google.gwt.core.shared.GWT;

public class EventBus {

  private final List<EventHandler<?, Object>> handlers = new LinkedList<EventHandler<?, Object>>();

  private final Queue<EventWithHandler<?, Object>> eventsToDispatch =
      new LinkedList<EventWithHandler<?, Object>>();
  private boolean isDispatching = false;

  private final List<ExceptionHandler> exceptionHandlers = new LinkedList<ExceptionHandler>();

  public <T> void post(T event) {
    if (event == null) {
      throw new NullPointerException();
    }

    for (EventHandler<?, Object> wildcardHandler : handlers) {
      @SuppressWarnings("unchecked")
      EventHandler<T, Object> handler = (EventHandler<T, Object>) wildcardHandler;
      eventsToDispatch.add(new EventWithHandler<T, Object>(event, handler));
    }

    if (!isDispatching) {
      dispatchQueuedEvents();
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void dispatchQueuedEvents() {
    isDispatching = true;
    EventWithHandler<T, Object> eventWithHandler;
    while ((eventWithHandler = (EventWithHandler<T, Object>) eventsToDispatch.poll()) != null) {
      EventHandler<T, Object> handler = eventWithHandler.handler;
      try {
        if (handler.method.acceptsArgument(eventWithHandler.event)) {
          handler.method.invoke(handler.owner, eventWithHandler.event);
        }
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

  public <T> void register(T owner, Class<? extends EventRegistration<T>> registrationClass) {
    EventRegistration<T> registration = GWT.create(registrationClass);
    for (Method<T, ?> wildcardMethod : registration.getMethods()) {
      @SuppressWarnings("unchecked")
      Method<T, Object> method = (Method<T, Object>) wildcardMethod;
      handlers.add(new EventHandler<T, Object>(owner, method));
    }
  }

  public void unregister(Object owner) {
    boolean removed = false;
    for (Iterator<EventHandler<?, Object>> it = handlers.iterator(); it.hasNext();) {
      if (owner == it.next().owner) {
        it.remove();
        removed = true;
      }
    }
    if (!removed) {
      throw new IllegalArgumentException("Object was never registered: " + owner);
    }
  }

  public void addExceptionHandler(ExceptionHandler exceptionHandler) {
    exceptionHandlers.add(exceptionHandler);
  }

  private class EventHandler<T, U> {
    T owner;
    Method<T, U> method;

    public EventHandler(T owner, Method<T, U> method) {
      this.owner = owner;
      this.method = method;
    }
  }

  private static class EventWithHandler<T, U> {
    final Object event;
    final EventHandler<T, U> handler;

    public EventWithHandler(Object event, EventHandler<T, U> handler) {
      this.event = event;
      this.handler = handler;
    }
  }
}
