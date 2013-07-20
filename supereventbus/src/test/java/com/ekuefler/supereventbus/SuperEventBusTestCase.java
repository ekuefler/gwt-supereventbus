/*
 * Copyright 2013 Erik Kuefler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ekuefler.supereventbus;

import java.util.LinkedList;
import java.util.List;

import com.ekuefler.supereventbus.EventBus;
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
