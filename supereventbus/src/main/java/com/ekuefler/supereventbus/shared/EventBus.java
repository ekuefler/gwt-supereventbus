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

  // No-op handler method used as a sentinel when handlers are removed
  private static final EventHandlerMethod<Object, Object> NULL_HANDLER_METHOD =
      new EventHandlerMethod<Object, Object>() {
        @Override
        public void invoke(Object instance, Object arg) {}

        @Override
        public boolean acceptsArgument(Object arg) {
          return false;
        }

        @Override
        public int getDispatchOrder() {
          return 0;
        }
      };

  // Map from priority numbers to a list of all event handlers registered at that priority
  private final Map<Integer, List<EventHandler<?, ?>>> allHandlersByPriority =
      new HashMap<Integer, List<EventHandler<?, ?>>>();

  // Cache of known event handlers for each event type. The cache for each event class keeps track
  // of all handlers for that event and when the global handler list was last checked. When an event
  // is fired, all new handlers added since the last time the event was fired are checked and added
  // to the cache as need be. This should mean that all dispatches after the first for a given event
  // type will be efficient so long as few new handlers were added.
  private final Map<Class<?>, CacheEntry<?>> handlerCache = new HashMap<Class<?>, CacheEntry<?>>();

  // A queue of events being dispatched. When one event fires another event, it is added to the
  // queue rather than dispatched immediately in order to preserve the order of events.
  private final Queue<EventWithHandler<?, ?, ?>> eventsToDispatch =
      new LinkedList<EventWithHandler<?, ?, ?>>();

  // Whether we are in the process of dispatching events
  private boolean isDispatching = false;

  // List of all exception handlers registered by the user
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

    // Look up the cache entry for the class of the given event, adding a new entry if this is the
    // first time an event of the class has been fired.
    if (!handlerCache.containsKey(event.getClass())) {
      handlerCache.put(event.getClass(), new CacheEntry<T>());
    }
    @SuppressWarnings("unchecked")
    CacheEntry<T> cacheEntry = (CacheEntry<T>) handlerCache.get(event.getClass());

    // Updates the cache for the given event class, ensuring that it contains all registered
    // handlers for that class. This time this takes will depend on the number of new event handlers
    // added since the last time an event of this type was fired.
    cacheEntry.update(event);

    // Queue up all handlers for this event
    for (EventHandler<?, ?> wildcardHandler : cacheEntry.getAllHandlers()) {
      @SuppressWarnings("unchecked")
      EventHandler<Object, Object> handler = (EventHandler<Object, Object>) wildcardHandler;
      eventsToDispatch.add(new EventWithHandler<T, Object, Object>(event, handler));
    }

    // Start dispatching the queued events. If we're already dispatching, it means that the handler
    // for one event posted another event, so we don't have to start dispatching again.
    if (!isDispatching) {
      dispatchQueuedEvents();
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void dispatchQueuedEvents() {
    isDispatching = true;
    try {
      // Dispatch all events in the queue, saving any exceptions for later
      EventWithHandler<T, Object, Object> eventWithHandler;
      List<EventBusException> exceptions = new LinkedList<EventBusException>();
      while ((eventWithHandler = (EventWithHandler<T, Object, Object>) eventsToDispatch.poll()) != null) {
        try {
          eventWithHandler.dispatch();
        } catch (Exception e) {
          exceptions.add(new EventBusException(
              e, eventWithHandler.handler.owner, eventWithHandler.event));
        }
      }

      // Notify all exception handlers of each exception
      for (EventBusException e : exceptions) {
        for (ExceptionHandler exceptionHandler : exceptionHandlers) {
          try {
            exceptionHandler.handleException(e);
          } catch (Exception ex) {
            GWT.log("Caught exception while handling an EventBusException, ignoring it", ex);
          }
        }
      }
    } finally {
      isDispatching = false;
    }
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

    // Add each handler method in the class to the global handler map according to its priority. The
    // cache mapping event classes to handler methods will be updated when an event is fired.
    for (EventHandlerMethod<T, ?> wildcardMethod : registration.getMethods()) {
      @SuppressWarnings("unchecked")
      EventHandlerMethod<T, Object> method = (EventHandlerMethod<T, Object>) wildcardMethod;
      if (!allHandlersByPriority.containsKey(method.getDispatchOrder())) {
        allHandlersByPriority.put(method.getDispatchOrder(), new ArrayList<EventHandler<?, ?>>());
      }
      allHandlersByPriority.get(method.getDispatchOrder()).add(
          new EventHandler<T, Object>(owner, method));
    }
  }

  /**
   * Unregisters all event handlers on the given object. After unregistering, {@link Subscribe}-
   * annotated methods on that object will never be invoked when an event is posted (unless the
   * object is registered again). This given object must have already been registered on the event
   * bus.
   *
   * @param owner object whose handlers should be disabled. Must already have been registered via a
   *          call to {@link #register}.
   * @throws IllegalArgumentException if the given object was never registered on this event bus
   */
  public void unregister(Object owner) {
    // First clear entries from the global handler list. We can't actually remove entries, since
    // this would break the indices stored in the cache. So replace removed entries with no-ops.
    boolean removed = false;
    for (List<EventHandler<?, ?>> handlerList : allHandlersByPriority.values()) {
      for (EventHandler<?, ?> handler : handlerList) {
        if (owner == handler.owner) {
          handler.nullify();
          removed = true;
        }
      }
    }

    // Ensure that something was actually removed
    if (!removed) {
      throw new IllegalArgumentException("Object was never registered: " + owner);
    }

    // Remove handlers from the cache
    for (CacheEntry<?> entry : handlerCache.values()) {
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

  /** A handler method combined with a specific instance of a class declaring that method. */
  private static class EventHandler<I, A> {
    I owner;
    EventHandlerMethod<I, A> method;

    EventHandler(I owner, EventHandlerMethod<I, A> method) {
      this.owner = owner;
      this.method = method;
    }

    @SuppressWarnings("unchecked")
    void nullify() {
      owner = null;
      method = (EventHandlerMethod<I, A>) NULL_HANDLER_METHOD;
    }
  }

  /** An event handler combined with a specific event to handle. */
  private static class EventWithHandler<E, I, A> {
    final E event;
    final EventHandler<I, A> handler;

    EventWithHandler(E event, EventHandler<I, A> handler) {
      this.event = event;
      this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public void dispatch() {
      handler.method.invoke(handler.owner, (A) event);
    }
  }

  /**
   * An entry in the handler cache for event classes, containing a list of known handlers and the
   * index of the last handler checked.
   */
  private class CacheEntry<T> {
    // Map from priority levels to a list of known event handlers for this type at that priority.
    // The map is updated whenever an event is fired.
    private final SortedMap<Integer, List<EventHandler<?, T>>> knownHandlersByPriority =
        new TreeMap<Integer, List<EventHandler<?, T>>>();

    // Map from priority levels to the last corresponding index in the global handler list that was
    // checked at that priority. When updating the cache, we continue from this index in order to
    // avoid having to re-scan entries that were already cached.
    private final Map<Integer, Integer> nextHandlerToCheckByPriority =
        new HashMap<Integer, Integer>();

    /** Updates this cache, ensuring it contains all handlers for the given event type. */
    void update(T event) {
      // Check each priority level in the global handler map
      for (Entry<Integer, List<EventHandler<?, ?>>> entry : allHandlersByPriority.entrySet()) {
        int priority = entry.getKey();
        List<EventHandler<?, ?>> handlers = entry.getValue();

        // Ensure that we have entries for this priority level if we don't already
        if (!knownHandlersByPriority.containsKey(priority)) {
          knownHandlersByPriority.put(priority, new LinkedList<EventHandler<?, T>>());
          nextHandlerToCheckByPriority.put(priority, 0);
        }

        // Starting with the last index we checked at this priority, advance to the end of the
        // global handler list for this priority.
        for (; nextHandlerToCheckByPriority.get(priority) < handlers.size(); increment(priority)) {
          int nextHandlerToCheck = nextHandlerToCheckByPriority.get(priority);
          @SuppressWarnings("unchecked")
          EventHandler<?, T> handler = (EventHandler<?, T>) handlers.get(nextHandlerToCheck);
          // Add this handler to the cache only if it is appropriate for the given event
          if (handler.method.acceptsArgument(event)) {
            knownHandlersByPriority.get(priority).add(handler);
          }
        }
      }
    }

    /** Returns all known handlers for this entry's event type, sorted by priority. */
    List<EventHandler<?, ?>> getAllHandlers() {
      List<EventHandler<?, ?>> result = new LinkedList<EventHandler<?, ?>>();
      for (List<EventHandler<?, T>> handlerList : knownHandlersByPriority.values()) {
        result.addAll(handlerList);
      }
      return result;
    }

    /** Removes all handlers registered on the given object from this cache entry. */
    void removeHandlersForOwner(Object owner) {
      for (List<EventHandler<?, T>> handlerList : knownHandlersByPriority.values()) {
        for (Iterator<EventHandler<?, T>> it = handlerList.iterator(); it.hasNext();) {
          if (owner == it.next().owner) {
            it.remove();
          }
        }
      }
    }

    // Increments the next handler to check index for the given priority level
    private void increment(int priority) {
      nextHandlerToCheckByPriority.put(priority, nextHandlerToCheckByPriority.get(priority) + 1);
    }
  }
}
