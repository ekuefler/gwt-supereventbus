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
 * A {@link DeadEvent} wraps an event that was posted to an {@link EventBus}, but for which no
 * subscribers were registered. Applications can subscribe to {@link DeadEvent} to help identify
 * misconfiguration issues. Note that if an event had a handler registered for it that was bypassed
 * due to an {@link com.ekuefler.supereventbus.shared.filtering.EventFilter}, a {@link DeadEvent}
 * will NOT be fired. Also note that if the application registers a handler for {@link Object}, all
 * events will be handled, and so {@link DeadEvent} will never be fired.
 *
 * @author ekuefler@gmail.com (Erik Kuefler)
 */
public class DeadEvent {

  private final Object event;

  DeadEvent(Object event) {
    this.event = event;
  }

  public Object getEvent() {
    return event;
  }
}
