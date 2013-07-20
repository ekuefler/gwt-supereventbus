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
import com.ekuefler.supereventbus.multievent.EventTypes;
import com.ekuefler.supereventbus.multievent.MultiEvent;

import java.util.LinkedList;
import java.util.List;

public class MultiEventTest extends SuperEventBusTestCase {

  class TestOwner {
    private final List<Object> stringsAndIntegers = new LinkedList<Object>();
    private final List<Object> objects = new LinkedList<Object>();

    @Subscribe
    void handleManyEvents(@EventTypes({String.class, Integer.class}) MultiEvent event) {
      stringsAndIntegers.add(event.getEvent());
    }

    @Subscribe
    void handleAllEvents(@EventTypes(Object.class) MultiEvent event) {
      objects.add(event.getEvent());
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

  public void testShouldReceiveManyEventTypes() {
    eventBus.post("hello world");
    eventBus.post(true);
    eventBus.post(123);
    eventBus.post(1.0);

    assertEquals(2, owner.stringsAndIntegers.size());
    assertEquals("hello world", owner.stringsAndIntegers.get(0));
    assertEquals(123, owner.stringsAndIntegers.get(1));
  }

  public void testShouldHandleEventsPolymorphically() {
    eventBus.post("hello world");
    eventBus.post(true);
    eventBus.post(123);
    eventBus.post(1.0);

    assertEquals(4, owner.objects.size());
  }

  public void testShouldNotAllowMultiEventToBePosted() {
    try {
      eventBus.post(new MultiEvent("not allowed"));
      fail("Exception not thrown");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("MultiEvent"));
    }
  }
}
