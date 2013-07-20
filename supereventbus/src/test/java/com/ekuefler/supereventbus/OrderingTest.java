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

import com.ekuefler.supereventbus.EventRegistration;
import com.ekuefler.supereventbus.Subscribe;

import java.util.LinkedList;
import java.util.List;

public class OrderingTest extends SuperEventBusTestCase {

  class TestOwner {
    final List<Object> events = new LinkedList<Object>();

    @Subscribe
    void handleInteger(Integer event) {
      eventBus.post("string1");
      events.add(event);
      eventBus.post("string2");
    }

    @Subscribe
    void handleString(String event) {
      eventBus.post('a');
      events.add(event);
      eventBus.post('b');
    }

    @Subscribe
    void handleChar(Character event) {
      events.add(event);
    }
  }

  interface MyRegistration extends EventRegistration<TestOwner> {}

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, MyRegistration.class);
  }

  public void testEventOrdering() {
    eventBus.post(123);

    assertEquals(listOf(123, "string1", "string2", 'a', 'b', 'a', 'b'), owner.events);
  }
}
