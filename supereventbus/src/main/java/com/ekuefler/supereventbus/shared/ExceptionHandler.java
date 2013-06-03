package com.ekuefler.supereventbus.shared;

/**
 * Marks an object as capable of handling exceptions thrown while dispatching events posted on the
 * event bus. After all handlers have had a chance to handle an event, any exceptions thrown by
 * handlers are collected, wrapped in an {@link EventBusException}, and passed in turn to all
 * registered exception handlers.
 *
 * @see {@link EventBus#addExceptionHandler}
 * @author ekuefler@google.com (Erik Kuefler)
 */
public interface ExceptionHandler {
  /**
   * Invoked whenever an exception occurs while handling an event, after all handlers have had a
   * chance to handle the event. The underlying exception will be wrapped in an
   * {@link EventBusException}, which provides access to the handler and event for which the
   * exception occurred. If multiple exceptions occurred while dispatching an event, this method
   * will be invoked multiple times.
   *
   * @param e a wrapper around the exception that occurred during event dispatch
   */
  void handleException(EventBusException e);
}
