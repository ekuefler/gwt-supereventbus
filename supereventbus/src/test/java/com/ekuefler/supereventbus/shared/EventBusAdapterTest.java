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

import com.ekuefler.supereventbus.shared.EventBusAdapterTest.TestOwner.MyRegistration;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.LinkedList;
import java.util.List;

public class EventBusAdapterTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private ClickEvent event;

    @Subscribe
    void onClickEvent(ClickEvent e) {
      this.event = e;
    }
  }

  private EventBusAdapter adapter;
  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    adapter = new EventBusAdapter(eventBus);
    owner = new TestOwner();
  }

  public void testFireOnAdapterShouldReceiveOnReal() {
    eventBus.register(owner, MyRegistration.class);
    adapter.fireEvent(new ClickEvent() {});

    assertNotNull(owner.event);
  }

  public void testPostOnRealShouldReceiveOnAdapter() {
    final List<ClickEvent> events = new LinkedList<ClickEvent>();
    adapter.addHandler(ClickEvent.getType(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        events.add(event);
      }
    });
    eventBus.post(new ClickEvent() {});

    assertEquals(1, events.size());
  }

  public void testFireOnAdapterShouldReceiveOnAdapter() {
    final List<ClickEvent> events = new LinkedList<ClickEvent>();
    adapter.addHandler(ClickEvent.getType(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        events.add(event);
      }
    });
    adapter.fireEvent(new ClickEvent() {});

    assertEquals(1, events.size());
  }

  public void testCanUnregisterHandlers() {
    HandlerRegistration registration = adapter.addHandler(ClickEvent.getType(), new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fail("Should never be called");
      }
    });
    registration.removeHandler();

    adapter.fireEvent(new ClickEvent() {});
    eventBus.post(new ClickEvent() {});
  }
}
