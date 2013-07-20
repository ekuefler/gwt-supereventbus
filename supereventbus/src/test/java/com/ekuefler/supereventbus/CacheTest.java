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

public class CacheTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    int eventsHandled = 0;

    @Subscribe
    void handleString(String event) {
      eventsHandled++;
    }
  }

  public void testShouldInvokeHandlersRegisteredAfterFiring() {
    TestOwner owner1 = new TestOwner();
    TestOwner owner2 = new TestOwner();

    eventBus.register(owner1, TestOwner.MyRegistration.class);
    eventBus.post("string");
    eventBus.register(owner2, TestOwner.MyRegistration.class);
    eventBus.post("string");

    assertEquals(2, owner1.eventsHandled);
    assertEquals(1, owner2.eventsHandled);
  }

  public void testShouldBeAbleToReRegisterHandlersAfterEventsAreFired() {
    TestOwner owner = new TestOwner();

    eventBus.register(owner, TestOwner.MyRegistration.class);
    eventBus.post("string");
    eventBus.unregister(owner);
    eventBus.register(owner, TestOwner.MyRegistration.class);
    eventBus.post("string");

    assertEquals(2, owner.eventsHandled);
  }

  public void testShouldBeAbleToReRegisterHandlersBeforeEventsAreFired() {
    TestOwner owner1 = new TestOwner();
    TestOwner owner2 = new TestOwner();
    eventBus.register(owner1, TestOwner.MyRegistration.class);
    eventBus.post("string");

    eventBus.register(owner2, TestOwner.MyRegistration.class);
    eventBus.unregister(owner2);
    eventBus.register(owner2, TestOwner.MyRegistration.class);
    eventBus.post("string");

    assertEquals(2, owner1.eventsHandled);
    assertEquals(1, owner2.eventsHandled);
  }
}
