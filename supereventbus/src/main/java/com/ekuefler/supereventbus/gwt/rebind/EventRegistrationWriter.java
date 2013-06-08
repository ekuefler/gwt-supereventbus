package com.ekuefler.supereventbus.gwt.rebind;

import com.ekuefler.supereventbus.shared.Subscribe;
import com.ekuefler.supereventbus.shared.filtering.When;
import com.ekuefler.supereventbus.shared.priority.WithPriority;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Writes implementations of {@link com.ekuefler.supereventbus.shared.EventRegistration}. The
 * generated class implements {@link com.ekuefler.supereventbus.shared.EventRegistration#getMethods}
 * by iterating over the target class's methods and generating an anonymous handler class for each
 * method annotated with {@link Subscribe}.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
class EventRegistrationWriter {

  /**
   * Writes the source for getMethods() the given target class to the given writer.
   */
  void writeGetMethods(JClassType target, SourceWriter writer) {
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

      // Add an anonymous instance of EventHandlerMethod for each method encountered
      String paramType = getFirstParameterType(method);
      writer.println("methods.add(new EventHandlerMethod<%s, %s>() {", targetType, paramType);
      writer.indent();
      {
        // Implement invoke() by calling the method, first checking filters if provided
        writer.println("public void invoke(%s instance, %s arg) {", targetType, paramType);
        if (method.getAnnotation(When.class) != null) {
          writer.indentln("if (%s) { instance.%s(arg); }", getFilter(method), method.getName());
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
        writer.indentln("return -1*%d;", method.getAnnotation(WithPriority.class) != null
            ? method.getAnnotation(WithPriority.class).value()
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
  private String getFilter(JMethod method) {
    StringBuilder predicate = new StringBuilder();
    When annotation = method.getAnnotation(When.class);
    boolean first = true;
    for (Class<?> filter : annotation.value()) {
      if (!first) {
        predicate.append("\n    && ");
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
}
