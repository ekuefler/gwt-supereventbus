package com.ekuefler.supereventbus.shared;

import java.util.List;

import com.ekuefler.supereventbus.shared.impl.EventHandlerMethod;

public interface EventRegistration<T> {
  List<EventHandlerMethod<T, ?>> getMethods();
}
