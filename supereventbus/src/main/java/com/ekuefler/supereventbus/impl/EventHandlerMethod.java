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
package com.ekuefler.supereventbus.impl;

import com.ekuefler.supereventbus.Subscribe;

/**
 * Internal representation of a {@link Subscribe}-annotated method. Users should not implement or
 * reference this interface - it is used internally and is subject to change.
 *
 * @author ekuefler@gmail.com (Erik Kuefler)
 *
 * @param <I> type of the instance in ehich the handler method is defined
 * @param <A> type of the argument to the handler method
 */
public interface EventHandlerMethod<I, A> {
  /**
   * Invokes the underlying method on the given instance.
   *
   * @param instance instance on which the method should be called
   * @param arg argument to pass to the instance
   */
  void invoke(I instance, A arg);

  /**
   * Returns whether or not the method accepts the given argument by checking its type. This does
   * NOT take filters into account.
   *
   * @param arg argument to check
   * @return <code>true</code> if the argument's type matches the argument type of the method
   */
  boolean acceptsArgument(Object arg);

  /**
   * Returns a key specifying the relative order in which this method should be invoked.
   * Lower-valued methods should be invoked before higher-valued methods.
   */
  int getDispatchOrder();
}
