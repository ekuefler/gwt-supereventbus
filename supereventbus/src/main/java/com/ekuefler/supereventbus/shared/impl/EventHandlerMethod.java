package com.ekuefler.supereventbus.shared.impl;

import com.ekuefler.supereventbus.shared.Subscribe;

/**
 * Internal representation of a {@link Subscribe}-annotated method. Users should not implement or
 * reference this interface - it is used internally and is subject to change.
 *
 * @author ekuefler@google.com (Erik Kuefler)
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
   * Returns the priority of this handler method. Higher-priority handlers will be invoked before
   * lower-priority handlers.
   */
  int getPriority();
}
