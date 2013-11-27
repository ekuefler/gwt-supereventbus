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
package com.ekuefler.supereventbus.rebind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ekuefler.supereventbus.Subscribe;
import com.ekuefler.supereventbus.filtering.EventFilter;
import com.ekuefler.supereventbus.filtering.When;
import com.ekuefler.supereventbus.multievent.EventTypes;
import com.ekuefler.supereventbus.multievent.MultiEvent;
import com.ekuefler.supereventbus.priority.WithPriority;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
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

  private @Mock JClassType target;
  private @Mock TreeLogger logger;
  private StringSourceWriter output;
  private EventRegistrationWriter writer;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    output = new StringSourceWriter();
    writer = new EventRegistrationWriter(logger);
  }

  @Test
  public void shouldWriteBasicHandler() throws Exception {
    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});
    when(target.getQualifiedSourceName()).thenReturn("MyType");

    writer.writeGetMethods(target, output);

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
        "}"), output.toString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldWriteHandlerWithFilter() throws Exception {
    When whenAnnotation = mock(When.class);
    when(whenAnnotation.value()).thenReturn(new Class[] {
        Filter1.class,
        Filter2.class,
    });

    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(method.getAnnotation(When.class)).thenReturn(whenAnnotation );
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});
    when(target.getQualifiedSourceName()).thenReturn("MyType");

    writer.writeGetMethods(target, output);

    assertContains(join(
        "    public void invoke(MyType instance, MyEvent arg) {",
        ("      if (new %s.Filter1().accepts(instance, arg) "
            + "&& new %s.Filter2().accepts(instance, arg)) { instance.myMethod(arg); }")
            .replaceAll("%s", EventRegistrationWriterTest.class.getCanonicalName()),
        "    }"), output.toString());
  }

  @Test
  public void shouldWriteHandlerWithPriority() throws Exception {
    WithPriority priorityAnnotation = mock(WithPriority.class);
    when(priorityAnnotation.value()).thenReturn(123);

    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(method.getAnnotation(WithPriority.class)).thenReturn(priorityAnnotation);
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});
    when(target.getQualifiedSourceName()).thenReturn("MyType");

    writer.writeGetMethods(target, output);

    assertContains(join(
        "    public int getDispatchOrder() {",
        "      return -123;",
        "    }"), output.toString());
  }

  @Test
  public void shouldWriteMultiEventHandlers() throws Exception {
    JParameter param = mock(JParameter.class);
    EventTypes typeAnnotation = mock(EventTypes.class);
    when(typeAnnotation.value()).thenReturn(new Class<?>[] {String.class, Integer.class});
    when(param.getAnnotation(EventTypes.class)).thenReturn(typeAnnotation);

    JMethod method = newSubscribeMethod(
        "myMethod", newEventType(MultiEvent.class.getCanonicalName()));
    when(method.getParameters()).thenReturn(new JParameter[] {param});
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});
    when(target.getQualifiedSourceName()).thenReturn("MyType");

    writer.writeGetMethods(target, output);

    assertContains(join(
        "  methods.add(new EventHandlerMethod<MyType, java.lang.String>() {",
        "    public void invoke(MyType instance, java.lang.String arg) {",
        "      instance.myMethod(new MultiEvent(arg));",
        "    }",
        "    public boolean acceptsArgument(Object arg) {",
        "      return arg instanceof java.lang.String;",
        "    }"), output.toString());
    assertContains(join(
        "  methods.add(new EventHandlerMethod<MyType, java.lang.Integer>() {",
        "    public void invoke(MyType instance, java.lang.Integer arg) {",
        "      instance.myMethod(new MultiEvent(arg));",
        "    }",
        "    public boolean acceptsArgument(Object arg) {",
        "      return arg instanceof java.lang.Integer;",
        "    }"), output.toString());
  }

  @Test(expected = UnableToCompleteException.class)
  public void shouldFailOnSubscribeMethodWithZeroArgs() throws Exception {
    JMethod method = mock(JMethod.class);
    when(method.getAnnotation(Subscribe.class)).thenReturn(mock(Subscribe.class));
    when(method.getParameterTypes()).thenReturn(new JType[] {});
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    writer.writeGetMethods(target, output);
  }

  @Test(expected = UnableToCompleteException.class)
  public void shouldFailOnSubscribeMethodWithTwoArgs() throws Exception {
    JMethod method = mock(JMethod.class);
    when(method.getAnnotation(Subscribe.class)).thenReturn(mock(Subscribe.class));
    when(method.getParameterTypes()).thenReturn(new JType[] {mock(JType.class), mock(JType.class)});
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    writer.writeGetMethods(target, output);
  }

  @Test(expected = UnableToCompleteException.class)
  public void shouldFailOnPrivateSubscribeMethod() throws Exception {
    JMethod method = mock(JMethod.class);
    when(method.getAnnotation(Subscribe.class)).thenReturn(mock(Subscribe.class));
    when(method.getParameterTypes()).thenReturn(new JType[] {mock(JType.class)});
    when(method.isPrivate()).thenReturn(true);
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    writer.writeGetMethods(target, output);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = UnableToCompleteException.class)
  public void shouldFailOnFilterWithoutZeroArgConstructor() throws Exception {
    When whenAnnotation = mock(When.class);
    when(whenAnnotation.value()).thenReturn(new Class[] {FilterWithoutZeroArgConstructor.class});

    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(method.getAnnotation(When.class)).thenReturn(whenAnnotation);
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    writer.writeGetMethods(target, output);
  }

  @Test(expected = UnableToCompleteException.class)
  public void shouldFailOnMultiEventWithoutTypes() throws Exception {
    JParameter param = mock(JParameter.class);
    when(param.getAnnotation(EventTypes.class)).thenReturn(null);

    JMethod method = newSubscribeMethod(
        "myMethod", newEventType(MultiEvent.class.getCanonicalName()));
    when(method.getParameters()).thenReturn(new JParameter[] {param});
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    writer.writeGetMethods(target, output);
  }

  @Test(expected = UnableToCompleteException.class)
  public void shouldFailOnNonMultiEventWithTypes() throws Exception {
    JParameter param = mock(JParameter.class);
    when(param.getAnnotation(EventTypes.class)).thenReturn(mock(EventTypes.class));

    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(method.getParameters()).thenReturn(new JParameter[] {param});
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    writer.writeGetMethods(target, output);
  }

  @Test(expected = UnableToCompleteException.class)
  public void shouldFailOnRedundantMultiEventTypes() throws Exception {
    JParameter param = mock(JParameter.class);
    EventTypes typeAnnotation = mock(EventTypes.class);
    when(typeAnnotation.value()).thenReturn(new Class<?>[] {String.class, Object.class});
    when(param.getAnnotation(EventTypes.class)).thenReturn(typeAnnotation);

    JMethod method = newSubscribeMethod("myMethod", newEventType("MyEvent"));
    when(method.getParameters()).thenReturn(new JParameter[] {param});
    when(target.getInheritableMethods()).thenReturn(new JMethod[] {method});

    writer.writeGetMethods(target, output);
  }

  private JMethod newSubscribeMethod(String name, JType paramType) {
    JMethod method = mock(JMethod.class);
    when(method.getName()).thenReturn(name);
    when(method.getAnnotation(Subscribe.class)).thenReturn(mock(Subscribe.class));
    when(method.getParameterTypes()).thenReturn(new JType[] {paramType});
    when(method.getParameters()).thenReturn(new JParameter[] {mock(JParameter.class)});
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

  public static class Filter1 implements EventFilter<Object, Object> {
    @Override
    public boolean accepts(Object handler, Object event) {
      return false;
    }
  }

  public static class Filter2 implements EventFilter<Object, Object> {
    @Override
    public boolean accepts(Object handler, Object event) {
      return false;
    }
  }

  public static class FilterWithoutZeroArgConstructor implements EventFilter<Object, Object> {
    public FilterWithoutZeroArgConstructor(String arg) {}

    @Override
    public boolean accepts(Object handler, Object event) {
      return false;
    }
  }
}
