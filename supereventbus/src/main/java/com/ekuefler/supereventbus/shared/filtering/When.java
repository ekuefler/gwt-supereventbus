package com.ekuefler.supereventbus.shared.filtering;

public @interface When {
  Class<? extends EventFilter<?>>[] value();
}
