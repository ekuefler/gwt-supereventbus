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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * An annotation that, when applied to a parameter of type {@link MultiEvent}, specifies which types
 * of events that method should listen for. See {@link MultiEvent} for more details and examples.
 * <p>
 * This annotation must be applied only to parameters of type {@link MultiEvent} on methods
 * annotated with {@link com.ekuefler.supereventbus.shared.Subscribe}.
 *
 * @author ekuefler@gmail.com (Erik Kuefler)
 */
@Documented
@Inherited
@Target(value = ElementType.PARAMETER)
public @interface EventTypes {
  /**
   * The list of types for which this event handler should listen. A posted event will cause the
   * underlying event handler to be invoked if an only if it is assignable to one of the types in
   * this list. Classes in this list must not be assignable to any other classes in this list.
   */
  Class<?>[] value();
}
