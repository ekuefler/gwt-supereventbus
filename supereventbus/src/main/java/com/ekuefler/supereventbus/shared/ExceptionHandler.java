package com.ekuefler.supereventbus.shared;

public interface ExceptionHandler {
  void handleException(Object event, Exception e);
}
