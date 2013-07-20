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
package com.ekuefler.supereventbus;

/**
 * An exception created when an event handler (a method annotated with {@link Subscribe}) fires an
 * exception during a call to {@link EventBus#post}. The underlying exception is available as the
 * cause of this exception. This event is not fired by {@link EventBus#post}, but is passed to any
 * exception handlers registered on the event bus via {@link EventBus#addExceptionHandler}.
 *
 * @author ekuefler@gmail.com (Erik Kuefler)
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