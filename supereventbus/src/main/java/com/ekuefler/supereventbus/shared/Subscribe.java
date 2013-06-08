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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * Annotates a method as an event handler. Whenever an event is posted on an event bus, all methods
 * with this annotation in all objects that have been registered on the event bus will be invoked if
 * they can handle the type of event being posted. An event handler method can handle a given event
 * if that event can be assigned to the single argument to that method. It is an error for a method
 * annotated with {@link Subscribe} to accept zero or more than one arguments.
 * <p>
 * By default, all event handlers are registered at priority 0 with no filtering. {@link Subscribe}
 * can be combined with {@link com.ekuefler.supereventbus.shared.priority.WithPriority} to override
 * the former and {@link com.ekuefler.supereventbus.shared.filtering.When} to override the latter.
 *
 * @see EventBus com.ekuefler.supereventbus.shared.priority.WithPriority
 *      com.ekuefler.supereventbus.shared.filtering.When
 * @author ekuefler@google.com (Erik Kuefler)
 */
@Documented
@Inherited
@Target(value = ElementType.METHOD)
public @interface Subscribe {}
