package com.ekuefler.supereventbus.shared.priority;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(value = ElementType.METHOD)
public @interface WithPriority {
  int value();
}
