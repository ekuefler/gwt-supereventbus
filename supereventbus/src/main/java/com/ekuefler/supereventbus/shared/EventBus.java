package com.ekuefler.supereventbus.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import com.ekuefler.supereventbus.shared.impl.Method;
import com.google.gwt.core.shared.GWT;

public class EventBus {

  private static final Method<Object, Object> NULL_METHOD = new Method<Object, Object>() {
    @Override
    public void invoke(Object instance, Object arg) {}

    @Override
    public boolean acceptsArgument(Object arg) {
      return false;
    }
  };

  private final List<EventHandler<?, ?>> globalHandlerList = new ArrayList<EventHandler<?, ?>>();
  private final Map<Class<?>, CacheEntry> handlerCache = new HashMap<Class<?>, CacheEntry>();

  private final Queue<EventWithHandler<?, ?>> eventsToDispatch =
      new LinkedList<EventWithHandler<?, ?>>();
  private boolean isDispatching = false;

  private final List<ExceptionHandler> exceptionHandlers = new LinkedList<ExceptionHandler>();

  public <T> void post(T event) {
    if (event == null) {
      throw new NullPointerException();
    }

    if (!handlerCache.containsKey(event.getClass())) {
      handlerCache.put(event.getClass(), new CacheEntry());
    }

    CacheEntry cacheEntry = handlerCache.get(event.getClass());
    int numGlobalHandlers = globalHandlerList.size();
    for (; cacheEntry.nextHandlerToCheck < numGlobalHandlers; cacheEntry.nextHandlerToCheck++) {
      EventHandler<?, ?> handler = globalHandlerList.get(cacheEntry.nextHandlerToCheck);
      if (handler.method.acceptsArgument(event)) {
        cacheEntry.knownHandlers.add(handler);
      }
    }

    for (EventHandler<?, ?> wildcardHandler : cacheEntry.knownHandlers) {
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
        handler.method.invoke(handler.owner, eventWithHandler.event);
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
      globalHandlerList.add(new EventHandler<T, Object>(owner, method));
    }
  }

  public void unregister(Object owner) {
    boolean removed = false;
    for (EventHandler<?, ?> handler : globalHandlerList) {
      if (owner == handler.owner) {
        handler.nullify();
        removed = true;
      }
    }

    if (!removed) {
      throw new IllegalArgumentException("Object was never registered: " + owner);
    }

    for (Entry<Class<?>, CacheEntry> entry : handlerCache.entrySet()) {
      CacheEntry cacheEntry = entry.getValue();
      for (Iterator<EventHandler<?, ?>> it = cacheEntry.knownHandlers.iterator(); it.hasNext();) {
        if (owner == it.next().owner) {
          it.remove();
        }
      }
    }
  }

  public void addExceptionHandler(ExceptionHandler exceptionHandler) {
    exceptionHandlers.add(exceptionHandler);
  }

  private static class EventHandler<T, U> {
    T owner;
    Method<T, U> method;

    EventHandler(T owner, Method<T, U> method) {
      this.owner = owner;
      this.method = method;
    }

    @SuppressWarnings("unchecked")
    void nullify() {
      owner = null;
      method = (Method<T, U>) NULL_METHOD;
    }
  }

  private static class EventWithHandler<T, U> {
    final Object event;
    final EventHandler<T, U> handler;

    EventWithHandler(Object event, EventHandler<T, U> handler) {
      this.event = event;
      this.handler = handler;
    }
  }

  private class CacheEntry {
    final List<EventHandler<?, ?>> knownHandlers = new LinkedList<EventHandler<?, ?>>();
    int nextHandlerToCheck = 0;
  }
}
