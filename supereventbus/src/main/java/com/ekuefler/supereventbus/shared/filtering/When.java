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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * Applies a filter to a {@link com.ekuefler.supereventbus.shared.Subscribe}-annotated method so
 * that the annotated method will be invoked only if the filter allows it. For example, the
 * following filter would only listen for events when the enclosing widget was visible:
 *
 * <pre>
 * class IsVisible implements EventFilter&lt;HasVisibility, Object&gt; {
 *   &#064;Override
 *   public boolean accepts(HasVisibility handler, Object event) {
 *     return handler.isVisible();
 *   }
 * }
 * </pre>
 *
 * That filter could be applied to a handler method as follows:
 *
 * <pre>
 * &#064;Subscribe &#064;When(IsVisible.class)
 * void onMyEvent(MyEvent event) {
 *   // Handle the event
 * }
 * </pre>
 *
 * Whenever MyEvent is fired, the handler method would be notified only if the widget containing it
 * was visible.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 * @see EventFilter, com.ekuefler.supereventbus.shared.Subscribe
 */
@Documented
@Inherited
@Target(value = ElementType.METHOD)
public @interface When {
  /**
   * The list of filters that must be passed before this method is invoked. If multiple filters are
   * specified, they must all pass in order for the method to be invoked. The check is
   * short-circuiting, so later filters will not be invoked if earlier filters fail.
   */
  Class<? extends EventFilter<?, ?>>[] value();
}
