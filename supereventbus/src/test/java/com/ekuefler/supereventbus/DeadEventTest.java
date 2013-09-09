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

import com.ekuefler.supereventbus.DeadEventTest.TestOwner.MyRegistration;
import com.ekuefler.supereventbus.filtering.EventFilter;
import com.ekuefler.supereventbus.filtering.When;
import com.google.gwt.core.client.GWT;

public class DeadEventTest extends SuperEventBusTestCase {

  public static class Never implements EventFilter<Object, Object> {
    @Override
    public boolean accepts(Object handler, Object event) {
      return false;
    }
  }

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private DeadEvent handledEvent;

    @Subscribe
    void handleInteger(int event) {}

    @Subscribe
    @When(Never.class)
    void handleDouble(double event) {}

    @Subscribe
    void handleDeadEvent(DeadEvent event) {
      this.handledEvent = event;
    }
  }

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, (MyRegistration) GWT.create(MyRegistration.class));
  }

  public void testShouldFireDeadEventForEventWithoutHandlers() throws Exception {
    eventBus.post("no handler");
    assertNotNull(owner.handledEvent);
    assertEquals("no handler", owner.handledEvent.getEvent().toString());
  }

  public void testShouldNotFireDeadEventForEventWithHandlers() throws Exception {
    eventBus.post(123);
    assertNull(owner.handledEvent);
  }

  public void testShouldNotFireDeadEventForFilteredEvent() throws Exception {
    eventBus.post(123.0);
    assertNull(owner.handledEvent);
  }
}
