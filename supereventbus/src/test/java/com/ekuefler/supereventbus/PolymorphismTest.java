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

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PolymorphismTest extends SuperEventBusTestCase {

  static class TestOwner {
    interface MyRegistration extends EventRegistration<TestOwner> {}

    private int objectsHandled = 0;
    private int listsHandled = 0;
    private int sequentialListsHandled = 0;
    private int linkedListsHandled = 0;

    @Subscribe
    void handleObject(Object event) {
      objectsHandled++;
    }

    @Subscribe
    void handleInterface(List<?> event) {
      listsHandled++;
    }

    @Subscribe
    void handleAbstractClass(AbstractSequentialList<?> event) {
      sequentialListsHandled++;
    }

    @Subscribe
    void handleConcreteType(LinkedList<?> event) {
      linkedListsHandled++;
    }
  }

  private TestOwner owner;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    owner = new TestOwner();
    eventBus.register(owner, (TestOwner.MyRegistration) GWT.create(TestOwner.MyRegistration.class));
  }

  public void testShouldHandleObjects() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(4, owner.objectsHandled);
  }

  public void testShouldHandleInterfaces() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(2, owner.listsHandled);
  }

  public void testShouldHandleAbstractClasses() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(1, owner.sequentialListsHandled);
  }

  public void testShouldHandleLinkedLists() {
    eventBus.post(123);
    eventBus.post("hello world");
    eventBus.post(new ArrayList<String>());
    eventBus.post(new LinkedList<String>());

    assertEquals(1, owner.linkedListsHandled);
  }
}
