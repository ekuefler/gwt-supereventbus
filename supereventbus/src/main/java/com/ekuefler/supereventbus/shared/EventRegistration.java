package com.ekuefler.supereventbus.shared;

import java.util.List;

import com.ekuefler.supereventbus.shared.impl.Method;

public interface EventRegistration<T> {
  List<Method<T, ?>> getMethods();
}
