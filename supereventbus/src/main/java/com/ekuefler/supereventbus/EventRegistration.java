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

import com.ekuefler.supereventbus.impl.EventHandlerMethod;

import java.util.List;

/**
 * A tagging interface associated with classes capable of handling events. Users should declare
 * subtypes of this interface corresponding to each class that should be registered on the event bus
 * - see the documentation of {@link EventBus} for details. Implementations of this interface are
 * generated by the GWT compiler - users should NOT implement this interface themselves. This
 * interface is subject to changes which may break user-defined implementations.
 *
 * @author ekuefler@gmail.com (Erik Kuefler)
 * @param <T> class to which this registration applies
 */
public interface EventRegistration<T> {
  /**
   * DO NOT IMPLEMENT THIS METHOD. Implementations are generated automatically by the GWT compiler,
   * and its details are subject to change.
   */
  List<EventHandlerMethod<T, ?>> getMethods();
}