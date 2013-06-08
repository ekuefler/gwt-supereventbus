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

import com.ekuefler.supereventbus.shared.filtering.EventFilter;
import com.ekuefler.supereventbus.shared.filtering.When;
import com.google.gwt.user.client.ui.HasVisibility;

import java.util.LinkedList;
import java.util.List;

public class FilteringTest extends SuperEventBusTestCase {

  static class IsGreaterThanTen implements EventFilter<Object, Integer> {
    @Override
    public boolean accepts(Object handler, Integer event) {
      return event > 10;
    }
  }

  static class IsLessThanTwenty implements EventFilter<Object, Integer> {
    @Override
    public boolean accepts(Object handler, Integer event) {
      return event < 20;
    }
  }

  static class IsVisible implements EventFilter<HasVisibility, Object> {
    @Override
    public boolean accepts(HasVisibility handler, Object event) {
      return handler.isVisible();
    }
  }

  static class TestOwner implements HasVisibility {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private final List<String> stringEvents = new LinkedList<String>();
    private int oneFilterValue = -1;
    private int twoFilterValue = -1;
    private boolean visible;

    @Subscribe
    @When(IsGreaterThanTen.class)
    void handleOneFilter(int event) {
      oneFilterValue = event;
    }

    @Subscribe
    @When({IsGreaterThanTen.class, IsLessThanTwenty.class})
    void handleTwoFilters(int event) {
      twoFilterValue = event;
    }

    @Subscribe
    @When(IsVisible.class)
    void handleStringWhenVisible(String event) {
      stringEvents.add(event);
    }

    @Override
    public boolean isVisible() {
      return visible;
    }

    @Override
    public void setVisible(boolean visible) {
      this.visible = visible;
    }
  }

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, TestOwner.MyRegistration.class);
  }

  public void testShouldApplySingleFilter() {
    eventBus.post(5); // Too small
    assertEquals(-1, owner.oneFilterValue);

    eventBus.post(15); // Just right
    assertEquals(15, owner.oneFilterValue);
  }

  public void testShouldApplyMultipleFilters() {
    eventBus.post(5); // Too small
    assertEquals(-1, owner.twoFilterValue);

    eventBus.post(15); // Just right
    assertEquals(15, owner.twoFilterValue);

    eventBus.post(25); // Too big
    assertEquals(15, owner.twoFilterValue);
  }

  public void testShouldApplyFiltersBasedOnHandler() {
    eventBus.post("before visible");
    owner.setVisible(true);
    eventBus.post("after visible");

    assertEquals(1, owner.stringEvents.size());
    assertEquals("after visible", owner.stringEvents.get(0));
  }
}
