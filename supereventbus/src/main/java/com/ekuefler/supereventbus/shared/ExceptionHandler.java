/*
 * Copyright 2013 Erik Kuefler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ekuefler.supereventbus.shared;

/**
 * Marks an object as capable of handling exceptions thrown while dispatching events posted on the
 * event bus. After all handlers have had a chance to handle an event, any exceptions thrown by
 * handlers are collected, wrapped in an {@link EventBusException}, and passed in turn to all
 * registered exception handlers.
 *
 * @see {@link EventBus#addExceptionHandler}
 * @author ekuefler@gmail.com (Erik Kuefler)
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
