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

import com.ekuefler.supereventbus.shared.Subscribe;
import com.ekuefler.supereventbus.shared.filtering.When;
import com.ekuefler.supereventbus.shared.priority.WithPriority;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;

import java.lang.reflect.Constructor;

/**
 * Writes implementations of {@link com.ekuefler.supereventbus.shared.EventRegistration}. The
 * generated class implements {@link com.ekuefler.supereventbus.shared.EventRegistration#getMethods}
 * by iterating over the target class's methods and generating an anonymous handler class for each
 * method annotated with {@link Subscribe}.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
class EventRegistrationWriter {

  private final TreeLogger logger;

  EventRegistrationWriter(TreeLogger logger) {
    this.logger = logger;
  }

  /**
   * Writes the source for getMethods() the given target class to the given writer.
   */
  void writeGetMethods(JClassType target, SourceWriter writer) throws UnableToCompleteException {
    String targetType = target.getQualifiedSourceName();
    writer.println("public List<EventHandlerMethod<%s, ?>> getMethods() {", targetType);
    writer.indent();
    writer.println("List<%1$s> methods = new LinkedList<%1$s>();",
        String.format("EventHandlerMethod<%s, ?>", targetType));

    // Iterate over each method in the target, looking for methods annotated with @Subscribe
    for (JMethod method : target.getMethods()) {
      if (method.getAnnotation(Subscribe.class) == null) {
        continue;
      }

      // Check method for validity
      if (method.getParameterTypes().length != 1) {
        logger.log(Type.ERROR,
            String.format("Method %s.%s annotated with @Subscribe must take exactly one argument.",
                target.getName(), method.getName()));
        throw new UnableToCompleteException();
      } else if (method.isPrivate()) {
        logger.log(Type.ERROR,
            String.format("Method %s.%s annotated with @Subscribe must not be private.",
                target.getName(), method.getName()));
        throw new UnableToCompleteException();
      }

      // Add an anonymous instance of EventHandlerMethod for each method encountered
      String paramType = getFirstParameterType(method);
      writer.println("methods.add(new EventHandlerMethod<%s, %s>() {", targetType, paramType);
      writer.indent();
      {
        // Implement invoke() by calling the method, first checking filters if provided
        writer.println("public void invoke(%s instance, %s arg) {", targetType, paramType);
        if (method.getAnnotation(When.class) != null) {
          writer.indent();
          writer.println("if (%s) {", getFilter(method));
          writer.indentln("instance.%s(arg);", method.getName());
          writer.println("}");
          writer.outdent();
        } else {
          writer.indentln("instance.%s(arg);", method.getName());
        }
        writer.println("}");

        // Implement acceptsArgument using instanceof
        writer.println("public boolean acceptsArgument(Object arg) {");
        writer.indentln("return arg instanceof %s;", paramType);
        writer.println("}");

        // Implement getDispatchOrder as the inverse of the method's priority
        writer.println("public int getDispatchOrder() {");
        writer.indentln("return %d;", method.getAnnotation(WithPriority.class) != null
            ? -method.getAnnotation(WithPriority.class).value()
            : 0);
        writer.println("}");
      }
      writer.outdent();
      writer.println("});");
    }
    writer.println("return methods;");
    writer.outdent();
    writer.println("}");
  }

  // Returns a boolean expression that should be used to check whether to invoke the given event
  // handler, based on the filters applied to it
  private String getFilter(JMethod method) throws UnableToCompleteException {
    StringBuilder predicate = new StringBuilder();
    When annotation = method.getAnnotation(When.class);
    boolean first = true;
    for (Class<?> filter : annotation.value()) {
      if (!classHasZeroArgConstructor(filter)) {
        logger.log(Type.ERROR, String.format(
            "Class %s extending EventFilter must define a public zero-argument constructor.",
            filter.getSimpleName()));
        throw new UnableToCompleteException();
      }
      if (!first) {
        predicate.append(" && ");
      }
      first = false;
      predicate.append(String.format(
          "new %s().accepts(instance, arg)", filter.getCanonicalName()));
    }
    return predicate.toString();
  }

  // Returns the type of the first parameter to the given method, boxed appropriately
  private String getFirstParameterType(JMethod method) {
    // If the parameter type is primitive, box it
    JType type = method.getParameterTypes()[0];
    if (type.isPrimitive() != null) {
      if (type.isPrimitive() == JPrimitiveType.BOOLEAN) {
        return Boolean.class.getName();
      } else if (type.isPrimitive() == JPrimitiveType.BYTE) {
        return Byte.class.getName();
      } else if (type.isPrimitive() == JPrimitiveType.CHAR) {
        return Character.class.getName();
      } else if (type.isPrimitive() == JPrimitiveType.DOUBLE) {
        return Double.class.getName();
      } else if (type.isPrimitive() == JPrimitiveType.FLOAT) {
        return Float.class.getName();
      } else if (type.isPrimitive() == JPrimitiveType.INT) {
        return Integer.class.getName();
      } else if (type.isPrimitive() == JPrimitiveType.LONG) {
        return Long.class.getName();
      } else if (type.isPrimitive() == JPrimitiveType.SHORT) {
        return Short.class.getName();
      }
    }

    // Otherwise return the fully-qualified type name
    return type.getQualifiedSourceName();
  }

  private boolean classHasZeroArgConstructor(Class<?> clazz) {
    try {
      for (Constructor<?> s : clazz.getConstructors()) {
        if (s.getParameterTypes().length == 0) {
          return true;
        }
      }
      return false;
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
  }
}
