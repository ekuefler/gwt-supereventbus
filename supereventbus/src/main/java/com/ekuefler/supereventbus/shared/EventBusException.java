package com.ekuefler.supereventbus.shared;

/**
 * An exception created when an event handler (a method annotated with {@link Subscribe}) fires an
 * exception during a call to {@link EventBus#post}. The underlying exception is available as the
 * cause of this exception. This event is not fired by {@link EventBus#post}, but is passed to any
 * exception handlers registered on the event bus via {@link EventBus#addExceptionHandler}.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public class EventBusException extends Exception {
  private final Object source;
  private final Object event;

  EventBusException(Exception cause, Object source, Object event) {
    super(cause);
    this.source = source;
    this.event = event;
  }

  /**
   * Returns the event passed to {@link EventBus#post} that caused the underlying exception to be
   * thrown.
   */
  public Object getEvent() {
    return event;
  }

  /**
   * Returns the object containing the {@link Subscribe}-annotated method that threw the underlying
   * exception.
   */
  public Object getSource() {
    return source;
  }
}