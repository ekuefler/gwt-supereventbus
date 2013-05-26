package com.ekuefler.supereventbus.shared.impl;

public interface Method<T, U> {
  void invoke(T instance, U arg);

  boolean acceptsArgument(Object arg);
}
