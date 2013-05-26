package com.ekuefler.supereventbus.shared;

public interface EventRegistration<T> {
  void dispatch(T owner, Object event);
}
