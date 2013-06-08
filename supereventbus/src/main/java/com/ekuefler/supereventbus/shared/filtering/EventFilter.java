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
package com.ekuefler.supereventbus.shared.filtering;

/**
 * A filter capable of preventing an event handler method from receiving events. Filters are applied
 * to event handlers using then {@link When} annotation. Filtering can be done based on the class
 * containing the event handler, the event itself, or a combination of the two.
 * <p>
 * Note that all event handlers MUST define a zero-argument public constructor.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 * @see When
 *
 * @param <H> type of the class containing the event handler method. This can be a general type like
 *          {@link Object}, in which case the filter can be applied in any class.
 * @param <E> type of event to which the filter applies. This can be a general type like
 *          {@link Object}, in which can the filter can be applied to any handler method.
 */
public interface EventFilter<H, E> {
  /**
   * Returns whether or not the given handler's {@link com.ekuefler.supereventbus.shared.Subscribe}
   * method should be invoked for the given event. If this method returns <code>false</code>, the
   * underlying event handler will never be called.
   *
   * @param handler class containing the {@link com.ekuefler.supereventbus.shared.Subscribe}-
   *          annotated method to which the event is about to be dispatched
   * @param event event currently being dispatched
   * @return <code>true</code> to allow the method to handle the event, <code>false</code> to
   *         prevent it
   */
  boolean accepts(H handler, E event);
}
