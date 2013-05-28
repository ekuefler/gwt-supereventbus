package com.ekuefler.supereventbus.gwt.rebind;

import com.ekuefler.supereventbus.shared.Subscribe;
import com.ekuefler.supereventbus.shared.filtering.When;
import com.ekuefler.supereventbus.shared.priority.WithPriority;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;

class EventRegistrationWriter {
  void writeGetMethods(JClassType target, SourceWriter writer) {
    String targetType = target.getQualifiedSourceName();
    writer.println("public List<EventHandlerMethod<%s, ?>> getMethods() {", targetType);
    writer.indent();
    writer.println("List<%1$s> methods = new LinkedList<%1$s>();",
        String.format("EventHandlerMethod<%s, ?>", targetType));
    for (JMethod method : target.getMethods()) {
      if (method.getAnnotation(Subscribe.class) == null) {
        continue;
      }
      String paramType = getFirstParameterType(method);
      writer.println("methods.add(new EventHandlerMethod<%s, %s>() {", targetType, paramType);
      writer.indent();
      {
        writer.println("public void invoke(%s instance, %s arg) {", targetType, paramType);
        if (method.getAnnotation(When.class) != null) {
          writer.indentln("if (%s) { instance.%s(arg); }", getFilter(method), method.getName());
        } else {
          writer.indentln("instance.%s(arg);", method.getName());
        }
        writer.println("}");

        writer.println("public boolean acceptsArgument(Object arg) {");
        writer.indentln("return arg instanceof %s;", paramType);
        writer.println("}");

        writer.println("public int getPriority() {");
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

  private String getFilter(JMethod method) {
    StringBuilder predicate = new StringBuilder();
    When annotation = method.getAnnotation(When.class);
    boolean first = true;
    if (annotation != null) {
      for (Class<?> filter : annotation.value()) {
        if (!first) {
          predicate.append("\n    && ");
        }
        first = false;
        predicate.append(String.format("new %s().accepts(arg)", filter.getCanonicalName()));
      }
    }
    return predicate.toString();
  }

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
