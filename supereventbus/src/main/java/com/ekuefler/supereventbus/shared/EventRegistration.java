package com.ekuefler.supereventbus.shared;

public interface EventRegistration<T> {
  void dispatch(Object owner, Object event);
}
