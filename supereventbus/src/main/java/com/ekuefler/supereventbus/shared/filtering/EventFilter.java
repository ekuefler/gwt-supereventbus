package com.ekuefler.supereventbus.shared.filtering;

public interface EventFilter<T> {
  boolean accepts(T event);
}
