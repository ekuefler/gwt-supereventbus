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
package com.ekuefler.supereventbus.gwt.rebind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ekuefler.supereventbus.shared.Subscribe;
import com.ekuefler.supereventbus.shared.filtering.EventFilter;
import com.ekuefler.supereventbus.shared.filtering.When;
import com.ekuefler.supereventbus.shared.priority.WithPriority;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.StringSourceWriter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link EventRegistrationWriter}. Most functionality should be tested via GWT tests;
 * these tests just ensure basic output format and check error cases.
 */
public class EventRegistrationWriterTest {

  private StringSourceWriter writer;
  private @Mock JClassType target;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    writer = new StringSourceWriter();
  }

  @Test
  public void shouldWriteBasicHandler() {
    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(target.getMethods()).thenReturn(new JMethod[] {method});
    when(target.getQualifiedSourceName()).thenReturn("MyType");

    new EventRegistrationWriter().writeGetMethods(target, writer);

    assertEquals(join(
        "public List<EventHandlerMethod<MyType, ?>> getMethods() {",
        "  List<EventHandlerMethod<MyType, ?>> methods = "
            + "new LinkedList<EventHandlerMethod<MyType, ?>>();",
        "  methods.add(new EventHandlerMethod<MyType, MyEvent>() {",
        "    public void invoke(MyType instance, MyEvent arg) {",
        "      instance.myMethod(arg);",
        "    }",
        "    public boolean acceptsArgument(Object arg) {",
        "      return arg instanceof MyEvent;",
        "    }",
        "    public int getDispatchOrder() {",
        "      return 0;",
        "    }",
        "  });",
        "  return methods;",
        "}"), writer.toString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldWriteHandlerWithFilter() {
    When whenAnnotation = mock(When.class);
    when(whenAnnotation.value()).thenReturn(new Class[] {
        Filter1.class,
        Filter2.class,
    });

    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(method.getAnnotation(When.class)).thenReturn(whenAnnotation );
    when(target.getMethods()).thenReturn(new JMethod[] {method});
    when(target.getQualifiedSourceName()).thenReturn("MyType");

    new EventRegistrationWriter().writeGetMethods(target, writer);

    assertContains(join(
        "    public void invoke(MyType instance, MyEvent arg) {",
        ("      if (new %s.Filter1().accepts(instance, arg) "
            + "&& new %s.Filter2().accepts(instance, arg)) {")
            .replaceAll("%s", EventRegistrationWriterTest.class.getCanonicalName()),
        "        instance.myMethod(arg);",
        "      }",
        "    }"), writer.toString());
  }

  @Test
  public void shouldWriteHandlerWithPriority() {
    WithPriority priorityAnnotation = mock(WithPriority.class);
    when(priorityAnnotation.value()).thenReturn(123);

    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(method.getAnnotation(WithPriority.class)).thenReturn(priorityAnnotation);
    when(target.getMethods()).thenReturn(new JMethod[] {method});
    when(target.getQualifiedSourceName()).thenReturn("MyType");

    new EventRegistrationWriter().writeGetMethods(target, writer);

    assertContains(join(
        "    public int getDispatchOrder() {",
        "      return -123;",
        "    }"), writer.toString());
  }

  private JMethod newSubscribeMethod(String name, JType paramType) {
    JMethod method = mock(JMethod.class);
    when(method.getName()).thenReturn(name);
    when(method.getAnnotation(Subscribe.class)).thenReturn(mock(Subscribe.class));
    when(method.getParameterTypes()).thenReturn(new JType[] {paramType});
    return method;
  }

  private JType newEventType(String name) {
    JType paramType = mock(JType.class);
    when(paramType.getQualifiedSourceName()).thenReturn(name);
    return paramType;
  }

  private static String join(String... strings) {
    StringBuilder result = new StringBuilder();
    for(String string : strings) {
      result.append(string).append('\n');
    }
    return result.toString();
  }

  private static void assertContains(String expected, String actual) {
    assertTrue(String.format("Expected <%s> in <%s>", expected, actual), actual.contains(expected));
  }

  private static class Filter1 implements EventFilter<Object, Object> {
    @Override
    public boolean accepts(Object handler, Object event) {
      return false;
    }
  }

  private static class Filter2 implements EventFilter<Object, Object> {
    @Override
    public boolean accepts(Object handler, Object event) {
      return false;
    }
  }
}
