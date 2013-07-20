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

import com.ekuefler.supereventbus.shared.impl.EventHandlerMethod;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.Event.Type;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * An adapter allowing SuperEventBus to be referenced via GWT's built-in
 * {@link com.google.web.bindery.event.shared.EventBus} interface. Calls to methods on this class
 * are forwarded to an underlying {@link EventBus}, effectively allowing an {@link EventBus} to be
 * passed to existing methods that expect a legacy
 * {@link com.google.web.bindery.event.shared.EventBus}. Since only a small subset of
 * {@link EventBus}'s functionality is exposed here, this should only be used for existing/legacy
 * code that can't be refactored to use {@link EventBus} directly.
 *
 * @author ekuefler@gmail.com (Erik Kuefler)
 */
public class EventBusAdapter extends com.google.web.bindery.event.shared.EventBus {

  private final EventBus eventBus;

  /** Creates a new adapter wrapping the given event bus. */
  public EventBusAdapter(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /** Invokes {@link EventBus#post} on the underlying event bus with the given event. */
  @Override
  public void fireEvent(Event<?> event) {
    eventBus.post(event);
  }

  /**
   * Emulates the behavior of registering a single handler method on the underlying event bus. The
   * handler is unfiltered and behaves as if it has priority zero.
   */
  @Override
  public <H> HandlerRegistration addHandler(final Type<H> type, final H handler) {
    eventBus.addHandlerMethod(handler, new EventHandlerMethod<Object, Event<H>>() {
      @Override
      public void invoke(Object instance, Event<H> arg) {
        dispatchEvent(arg, handler);
      }

      @Override
      public boolean acceptsArgument(Object arg) {
        return arg instanceof Event && ((Event<?>) arg).getAssociatedType() == type;
      }

      @Override
      public int getDispatchOrder() {
        return 0;
      }
    });
    return new HandlerRegistration() {
      @Override
      public void removeHandler() {
        eventBus.unregister(handler);
      }
    };
  }

  /** Not supported. */
  @Override
  public <H> HandlerRegistration addHandlerToSource(Type<H> type, Object source, H handler) {
    throw new UnsupportedOperationException();
  }

  /** Not supported. */
  @Override
  public void fireEventFromSource(Event<?> event, Object source) {
    throw new UnsupportedOperationException();
  }
}
