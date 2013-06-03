package com.ekuefler.supereventbus.shared.filtering;

public interface EventFilter<H, E> {
  boolean accepts(H handler, E event);
}
