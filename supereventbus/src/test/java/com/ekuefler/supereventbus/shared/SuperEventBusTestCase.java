package com.ekuefler.supereventbus.shared;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.junit.client.GWTTestCase;

public class SuperEventBusTestCase extends GWTTestCase {

  protected EventBus eventBus;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    eventBus = new EventBus();
  }

  @Override
  public String getModuleName() {
    return "com.ekuefler.supereventbus.SuperEventBus";
  }

  protected List<Object> listOf(Object... objects) {
    LinkedList<Object> list = new LinkedList<Object>();
    for (Object object : objects) {
      list.add(object);
    }
    return list;
  }
}
