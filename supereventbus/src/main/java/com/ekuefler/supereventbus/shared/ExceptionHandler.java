package com.ekuefler.supereventbus.shared;

import com.ekuefler.supereventbus.shared.EventBus.EventBusException;

public interface ExceptionHandler {
  void handleException(EventBusException e);
}
