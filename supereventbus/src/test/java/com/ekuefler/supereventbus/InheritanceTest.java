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

import com.google.gwt.core.client.GWT;

public class InheritanceTest extends SuperEventBusTestCase {

  static class Superclass {
    String stringInSuperclass;
    int integerInSuperclass;
    char characterInSuperclass;

    @Subscribe
    void handleString(String event) {
      stringInSuperclass = event;
    }

    @Subscribe
    void handleInteger(int event) {
      integerInSuperclass = event;
    }

    @Subscribe
    void handleCharacter(char event) {
      characterInSuperclass = event;
    }
  }

  static class Subclass extends Superclass {
    String stringInSubclass;
    int integerInSubclass;
    double doubleInSubclass;

    @Override
    @Subscribe
    void handleString(String event) {
      stringInSubclass = event;
    }

    @Subscribe
    void handleAnotherInteger(int event) {
      integerInSubclass = event;
    }

    @Subscribe
    void handleDouble(double event) {
      doubleInSubclass = event;
    }
  }

  interface MyRegistration extends EventRegistration<Subclass> {}

  private Subclass owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new Subclass();
    eventBus.register(owner, (MyRegistration) GWT.create(MyRegistration.class));
  }

  public void testShouldDispatchOverriddenEventOnlyToSubclass() {
    eventBus.post("hello world");
    assertEquals("hello world", owner.stringInSubclass);
    assertNull(owner.stringInSuperclass);
  }

  public void testShouldDispatchOverloadedEventToBothClasses() {
    eventBus.post(123);
    assertEquals(123, owner.integerInSubclass);
    assertEquals(123, owner.integerInSuperclass);
  }

  public void testShouldDispatchSuperclassOnlyEventToSuperclass() {
    eventBus.post('a');
    assertEquals('a', owner.characterInSuperclass);
  }

  public void testShouldDispatchSubclassOnlyEventToSubclass() {
    eventBus.post(123.0);
    assertEquals(123.0, owner.doubleInSubclass);
  }
}
