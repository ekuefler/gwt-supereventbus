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
package com.ekuefler.supereventbus.shared.multievent;

/**
 * An event wrapping another event that could be one of several unrelated types. This allows a
 * handler method to handle events of multiple types that are not assignable to each other. For
 * example, the following method would be invoked whenever Strings or Doubles were posted on the
 * event bus:
 *
 * <pre>
 * &#064;Subscribe
 * void handleMultipleTypes(&#064;EventTypes({String.class, Double.class}) MultiEvent event) {
 *   if (event instanceof String) {
 *     Window.alert(&quot;Got a string: &quot; + event.getEvent());
 *   } else if (event instanceof Double) {
 *     Window.alert(&quot;Got a double: &quot; + event.getEvent());
 *   }
 * }
 * </pre>
 *
 * Note the use of the {@link EventTypes} annotation to indicate which types of events should be
 * handled. Parameters of type {@link MultiEvent} in Subscribe-annotated methods must always contain
 * this annotation.
 * <p>
 * Also note that this technique should be used relatively sparingly. Most of the time, events that
 * should be handled in the same way should be made to extend a common base class or interface, in
 * which case that type can be made the argument to the handler method instead of MultiEvent.
 * MultiEvent should only be used to handle events of unrelated types that can't be made to extend a
 * common base.
 * <p>
 * Instances of MultiEvent are created automatically by the event bus - users should never
 * instantiate or post these events on the event bus.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 * @see EventTypes
 */
public class MultiEvent {

  private final Object event;

  /**
   * Instantiates a new MultiEvent wrapping the given event. Users should never have to invoke this
   * method directly.
   */
  public MultiEvent(Object event) {
    this.event = event;
  }

  /**
   * Returns the underlying event that this event wraps, which will be of a type assignable to one
   * of the types declared in the {@link EventTypes} annotation for this parameter.
   */
  public Object getEvent() {
    return event;
  }
}
