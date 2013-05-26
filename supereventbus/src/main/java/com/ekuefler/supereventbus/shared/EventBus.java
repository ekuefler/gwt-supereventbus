package com.ekuefler.supereventbus.shared;

import java.util.Iterator;
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

  public <T> void register(T owner, Class<? extends EventRegistration<T>> registrationClass) {
    EventRegistration<T> registration = GWT.create(registrationClass);
    handlers.add(new EventHandler<T>(owner, registration));
  }

  public void unregister(Object owner) {
    boolean removed = false;
    for (Iterator<EventHandler<?>> it = handlers.iterator(); it.hasNext();) {
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

  private class EventHandler<T> {
    T owner;
    EventRegistration<T> registration;

    public EventHandler(T owner, EventRegistration<T> registration) {
      this.owner = owner;
      this.registration = registration;
    }
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
