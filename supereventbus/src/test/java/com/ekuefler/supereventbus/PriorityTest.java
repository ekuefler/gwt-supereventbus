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

import com.ekuefler.supereventbus.priority.WithPriority;
import com.google.gwt.core.client.GWT;

public class PriorityTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private final List<Integer> handledPriorities = new LinkedList<Integer>();

    @Subscribe
    @WithPriority(-10)
    void priortyNeg10(String event) {
      handledPriorities.add(-10);
    }

    @Subscribe
    @WithPriority(10)
    void priorty10(String event) {
      handledPriorities.add(10);
    }

    @Subscribe
    @WithPriority(20)
    void priorty20(String event) {
      handledPriorities.add(20);
    }

    @Subscribe
    @WithPriority(-20)
    void priortyNeg20(String event) {
      handledPriorities.add(-20);
    }

    @Subscribe
    void priorty0(String event) {
      handledPriorities.add(0);
    }
  }

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, (TestOwner.MyRegistration) GWT.create(TestOwner.MyRegistration.class));
  }

  public void testShouldHandleInPriorityOrder() {
    eventBus.post("event");
    assertEquals(listOf(20, 10, 0, -10, -20), owner.handledPriorities);
  }
}
