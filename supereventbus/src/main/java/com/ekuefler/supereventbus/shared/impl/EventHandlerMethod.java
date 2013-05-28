package com.ekuefler.supereventbus.shared.impl;

public interface EventHandlerMethod<T, U> {
  void invoke(T instance, U arg);

  boolean acceptsArgument(Object arg);

  int getPriority();
}