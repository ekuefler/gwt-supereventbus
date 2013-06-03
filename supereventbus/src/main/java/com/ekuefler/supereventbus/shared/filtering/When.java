package com.ekuefler.supereventbus.shared.filtering;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(value = ElementType.METHOD)
public @interface When {
  Class<? extends EventFilter<?, ?>>[] value();
}
