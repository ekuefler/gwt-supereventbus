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
package com.ekuefler.supereventbus.shared;

import java.util.List;

public class BasicTest extends SuperEventBusTestCase {

  class TestOwner {

    private String string;
    private int integer;
    private String[] stringArray;
    private List<String> listOfString;

    @Subscribe
    void handleString(String event) {
      string = event;
    }

    @Subscribe
    void handleInteger(int event) {
      integer = event;
    }

    @Subscribe
    void handleStringArray(String[] event) {
      stringArray = event;
    }

    @Subscribe
    void handleListOfString(List<String> event) {
      listOfString = event;
    }

    @Subscribe
    void handleChar(char event) {
      eventBus.unregister(this);
      eventBus.post("Should never see this");
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

  public void testShouldDispatchObjects() {
    eventBus.post("hello world");
    assertEquals("hello world", owner.string);
  }

  public void testShouldDispatchPrimitives() {
    eventBus.post(123);
    assertEquals(123, owner.integer);
  }

  public void testShouldDispatchBoxedPrimitives() {
    eventBus.post(new Integer(123));
    assertEquals(123, owner.integer);
  }

  public void testShouldDispatchArrays() {
    eventBus.post(new String[] {"hello", "world"});

    assertEquals(2, owner.stringArray.length);
    assertEquals("hello", owner.stringArray[0]);
    assertEquals("world", owner.stringArray[1]);
  }

  public void testShouldDispatchGenericTypes() {
    eventBus.post(listOf("hello", "world"));
    assertEquals(listOf("hello", "world"), owner.listOfString);
  }

  public void testShouldNotDispatchEventsAfterUnregistering() {
    eventBus.post("before");
    eventBus.unregister(owner);
    eventBus.post("after");

    assertEquals("before", owner.string);
  }

  public void testShouldAllowUnregisteringInEventHandler() {
    eventBus.post("before");
    eventBus.post('a'); // Triggers a call to unregister
    eventBus.post("after");

    assertEquals("before", owner.string);
  }

  public void testShouldThrowExceptionWhenUnregisteringObjectNotRegistered() {
    try {
      eventBus.unregister("something");
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  public void testShouldThrowExceptionWhenUnregisteringTwice() {
    eventBus.unregister(owner);
    try {
      eventBus.unregister(owner);
      fail();
    } catch (IllegalArgumentException expected) {}
  }
}
