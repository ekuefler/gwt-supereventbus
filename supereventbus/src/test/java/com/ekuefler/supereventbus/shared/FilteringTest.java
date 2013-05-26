package com.ekuefler.supereventbus.shared;

import com.ekuefler.supereventbus.shared.filtering.EventFilter;
import com.ekuefler.supereventbus.shared.filtering.When;

public class FilteringTest extends SuperEventBusTestCase {

  static class IsGreaterThanTen implements EventFilter<Integer> {
    @Override
    public boolean accepts(Integer event) {
      return event > 10;
    }
  }

  static class IsLessThanTwenty implements EventFilter<Integer> {
    @Override
    public boolean accepts(Integer event) {
      return event < 20;
    }
  }

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private int oneFilterValue = -1;
    private int twoFilterValue = -1;

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
}
