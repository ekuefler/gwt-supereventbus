package com.ekuefler.supereventbus.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ekuefler.supereventbus.shared.impl.EventHandlerMethod;
import com.google.gwt.core.shared.GWT;

public class EventBus {

  private static final EventHandlerMethod<Object, Object> NULL_METHOD = new EventHandlerMethod<Object, Object>() {
    @Override
    public void invoke(Object instance, Object arg) {}

    @Override
    public boolean acceptsArgument(Object arg) {
      return false;
    }

    @Override
    public int getPriority() {
      return 0;
    }
  };

  private final Map<Integer, List<EventHandler<?, ?>>> allHandlersByPriority =
      new HashMap<Integer, List<EventHandler<?, ?>>>();
  private final Map<Class<?>, CacheEntry> handlerCache = new HashMap<Class<?>, CacheEntry>();

  private final Queue<EventWithHandler<?, ?>> eventsToDispatch =
      new LinkedList<EventWithHandler<?, ?>>();
  private boolean isDispatching = false;

  private final List<ExceptionHandler> exceptionHandlers = new LinkedList<ExceptionHandler>();

  public EventBus() {
    if (!GWT.isProdMode()) {
      addExceptionHandler(new ExceptionHandler() {
        @Override
        public void handleException(Object event, Exception e) {
          GWT.log("Got exception when posting event '" + event + "': " + e);
        }
      });
    }
  }

  public <T> void post(T event) {
    if (event == null) {
      throw new NullPointerException();
    }

    if (!handlerCache.containsKey(event.getClass())) {
      handlerCache.put(event.getClass(), new CacheEntry());
    }
    CacheEntry cacheEntry = handlerCache.get(event.getClass());

    cacheEntry.update(event);
    for (EventHandler<?, ?> wildcardHandler : cacheEntry.getAllHandlers()) {
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
            exceptionHandler.handleException(eventWithHandler.event, e);
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
    for (EventHandlerMethod<T, ?> wildcardMethod : registration.getMethods()) {
      @SuppressWarnings("unchecked")
      EventHandlerMethod<T, Object> method = (EventHandlerMethod<T, Object>) wildcardMethod;
      if (!allHandlersByPriority.containsKey(method.getPriority())) {
        allHandlersByPriority.put(method.getPriority(), new ArrayList<EventHandler<?, ?>>());
      }
      allHandlersByPriority.get(method.getPriority()).add(
          new EventHandler<T, Object>(owner, method));
    }
  }

  public void unregister(Object owner) {
    boolean removed = false;
    for (List<EventHandler<?, ?>> handlerList : allHandlersByPriority.values()) {
      for (EventHandler<?, ?> handler : handlerList) {
        if (owner == handler.owner) {
          handler.nullify();
          removed = true;
        }
      }
    }

    if (!removed) {
      throw new IllegalArgumentException("Object was never registered: " + owner);
    }

    for (CacheEntry entry : handlerCache.values()) {
      entry.removeHandlersForOwner(owner);
    }
  }

  public void addExceptionHandler(ExceptionHandler exceptionHandler) {
    exceptionHandlers.add(exceptionHandler);
  }

  private static class EventHandler<T, U> {
    T owner;
    EventHandlerMethod<T, U> method;

    EventHandler(T owner, EventHandlerMethod<T, U> method) {
      this.owner = owner;
      this.method = method;
    }

    @SuppressWarnings("unchecked")
    void nullify() {
      owner = null;
      method = (EventHandlerMethod<T, U>) NULL_METHOD;
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
    private final SortedMap<Integer, List<EventHandler<?, ?>>> knownHandlersByPriority =
        new TreeMap<Integer, List<EventHandler<?, ?>>>();
    private final Map<Integer, Integer> nextHandlerToCheckByPriority =
        new HashMap<Integer, Integer>();

    void update(Object event) {
      for (Entry<Integer, List<EventHandler<?, ?>>> entry : allHandlersByPriority.entrySet()) {
        int priority = entry.getKey();
        List<EventHandler<?, ?>> handlers = entry.getValue();
        if (!knownHandlersByPriority.containsKey(priority)) {
          knownHandlersByPriority.put(priority, new LinkedList<EventHandler<?, ?>>());
          nextHandlerToCheckByPriority.put(priority, 0);
        }
        for (; nextHandlerToCheckByPriority.get(priority) < handlers.size(); increment(priority)) {
          EventHandler<?, ?> handler = handlers.get(nextHandlerToCheckByPriority.get(priority));
          if (handler.method.acceptsArgument(event)) {
            knownHandlersByPriority.get(priority).add(handler);
          }
        }
      }
    }

    List<EventHandler<?, ?>> getAllHandlers() {
      List<EventHandler<?, ?>> result = new LinkedList<EventHandler<?, ?>>();
      for (List<EventHandler<?, ?>> handlerList : knownHandlersByPriority.values()) {
        result.addAll(handlerList);
      }
      return result;
    }

    void removeHandlersForOwner(Object owner) {
      for (List<EventHandler<?, ?>> handlerList : knownHandlersByPriority.values()) {
        for (Iterator<EventHandler<?, ?>> it = handlerList.iterator(); it.hasNext();) {
          if (owner == it.next().owner) {
            it.remove();
          }
        }
      }
    }

    private void increment(int priority) {
      nextHandlerToCheckByPriority.put(priority, nextHandlerToCheckByPriority.get(priority) + 1);
    }
  }
}
