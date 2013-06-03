package com.ekuefler.supereventbus.shared;

import com.ekuefler.supereventbus.shared.impl.EventHandlerMethod;
import com.google.gwt.core.shared.GWT;

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

/**
 * An event bus implementation for GWT that is significantly more powerful than the built-in
 * {@link com.google.web.bindery.event.shared.EventBus}. Features provided by this event bus over
 * the built-in one include the following:
 * <ol>
 * <li><b>Declarative handler registration</b> - instead of using events to register individual
 * handlers, methods can be annotated with {@link Subscribe} and are all automatically registered in
 * a single bind step.
 *
 * <li><b>Flexible typing</b> - with GWT's event bus, all events must extend
 * {@link com.google.gwt.event.shared.GwtEvent} and implement associated boilerplate. With
 * SuperEventBus, any object can be fired on the bus, even primitive types.
 *
 * <li><b>Polymorphism</b> - GWT's event type are monomorphic, which means that a handler for
 * MyEvent would never see any subclasses of MyEvent. In contrast, SuperEventBus treats events
 * polymorphically, so a handler for MyEvent would see any subclass of MyEvent. This allows you to
 * define hierarchies of events and handle them together, define tagging interfaces to mark groups
 * of events that should be handled in the same way, or event define a handle for {@link Object} to
 * handle all events fired by the system.
 *
 * <li><b>Better-behaved event dispatch</b> - Using the GWT event bus, firing an event from another
 * event handler will cause that new event to be processed before processing of the original event
 * completes. This means that other components listening for both events will see them in an
 * undefined order. SuperEventBus uses a queue to dispatch events, so if event A is fired before
 * event B, handlers we always receive event A before receiving event B.
 *
 * <li><b>Handler priorities</b> - handlers on the GWT event bus are always invoked in an undefined
 * order. With SuperEventBus, you can use the
 * {@link com.ekuefler.supereventbus.shared.priority.WithPriority} annotation to force some handlers
 * to be run before other handlers.
 *
 * <li><b>Filtering</b> - handlers on the GWT event bus will always be invoked whenever they are
 * registered. With SuperEventBus, you can use the
 * {@link com.ekuefler.supereventbus.shared.filtering.When} annotation to conditionally disable
 * certain handlers, based on either properties of the handler or based on the event being handled.
 * </ol>
 *
 * To register handlers on the event bus, you must first declare an {@link EventRegistration}
 * interface for the type of the handler like this:
 *
 * <pre>
 * interface MyRegistration extends EventRegistration&lt;TestOwner&gt; {}
 * </pre>
 *
 * This is necessary so that the GWT compiler can generate dispatch code specific to the handler
 * class - you should not implement this interface yourself. Once this interface is defined, you can
 * register an object to listen on the event bus like this:
 *
 * <pre>
 * eventBus.register(this, MyRegistration.class);
 * </pre>
 *
 * This will cause all {@link Subscribe}-annotated methods on the current object to be invoked
 * whenever an appropriate event is fired. Methods annotated with {@link Subscribe} must take a
 * single argument, which specifies the type of event to handle. An event can be any Java object,
 * and a handler for a given type will be invoked whenever an object of that type or its subclasses
 * is posted on the event bus. To post an object to an event bus, simply call
 *
 * <pre>
 * eventBus.post(new MyEvent(&quot;some data&quot;));
 * </pre>
 *
 * @see Subscribe, com.ekuefler.supereventbus.shared.filtering.When,
 *      com.ekuefler.supereventbus.shared.priority.WithPriority
 * @author ekuefler@google.com (Erik Kuefler)
 */
public class EventBus {

  private static final EventHandlerMethod<Object, Object> NULL_METHOD =
      new EventHandlerMethod<Object, Object>() {
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

  /**
   * Creates a new event bus. In dev mode, any exceptions that occur while dispatching events will
   * be logged with {@link GWT#log}. In prod mode, exceptions are silently ignored unless a handler
   * is added via {@link #addExceptionHandler}.
   */
  public EventBus() {
    if (!GWT.isProdMode()) {
      addExceptionHandler(new ExceptionHandler() {
        @Override
        public void handleException(EventBusException e) {
          GWT.log("Got exception when handling event \"" + e.getEvent() + "\"", e.getCause());
        }
      });
    }
  }

  /**
   * Posts the given event to all handlers registered on this event bus. This method will return
   * after the event has been posted to all handlers and will never throw an exception. After the
   * event has been posted, any exceptions thrown by handlers of the event are collected and passed
   * to each exception handler registered via {@link #addExceptionHandler(ExceptionHandler)}.
   *
   * @param event event object to post to all handlers
   */
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
    List<EventBusException> exceptions = new LinkedList<EventBusException>();
    while ((eventWithHandler = (EventWithHandler<T, Object>) eventsToDispatch.poll()) != null) {
      EventHandler<T, Object> handler = eventWithHandler.handler;
      try {
        handler.method.invoke(handler.owner, eventWithHandler.event);
      } catch (Exception e) {
        exceptions.add(new EventBusException(e, handler.owner, eventWithHandler.event));
      }
    }
    for (EventBusException e : exceptions) {
      for (ExceptionHandler exceptionHandler : exceptionHandlers) {
        try {
          exceptionHandler.handleException(e);
        } catch (Exception ex) {
          GWT.log("Caught exception while handling an EventBusException, ignoring it", ex);
        }
      }
    }
    isDispatching = false;
  }

  /**
   * Registers all {@link Subscribe}-annotated in the given object on the event bus. Any methods
   * annotated with {@link Subscribe} must take a single argument specifying the event to handle.
   * After an object has been registered, whenever an event is posted on the event bus via
   * {@link #post}, all handlers on that object for that event's type or its supertypes will be
   * invoked with that event.
   *
   * @param owner object to scan for {@link Subscribe}-annotated methods to register
   * @param registrationClass the class object of a registration interface for the given owner
   */
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

  /**
   * Unregisters all event handlers on the given object. After unregistering, {@link Subscribe}
   * -annotated methods on that object will never be invoked when an event is posted (unless the
   * object is registered again). This given object must have already been registered on the event
   * bus.
   *
   * @param owner object whose handlers should be disabled. Must already have been registered via a
   *          call to {@link #register}.
   * @throws IllegalArgumentException if the given object was never registered on this event bus
   */
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

  /**
   * Adds an exception handler to be notified whenever an exception occurs while dispatching an
   * event. Exception handlers are invoked only after all handlers have had a chance to process an
   * event - at that point, every exception that occurred during dispatch is wrapped in an
   * {@link EventBusException} and posted to every exception handler.
   *
   * @param exceptionHandler handler to be notified when exceptions occur
   */
  public void addExceptionHandler(ExceptionHandler exceptionHandler) {
    exceptionHandlers.add(exceptionHandler);
  }

  public static class EventBusException extends Exception {
    private final Object source;
    private final Object event;

    EventBusException(Exception cause, Object source, Object event) {
      super(cause);
      this.source = source;
      this.event = event;
    }

    public Object getEvent() {
      return event;
    }

    public Object getSource() {
      return source;
    }
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
