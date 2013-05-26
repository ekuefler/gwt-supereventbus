package com.ekuefler.supereventbus.gwt.rebind;

import com.ekuefler.supereventbus.shared.Subscribe;
import com.ekuefler.supereventbus.shared.filtering.When;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;

class EventRegistrationWriter {
  void writeDispatch(JClassType target, SourceWriter writer) {
    writer.println("public void dispatch(%s owner, Object event) {",
        target.getQualifiedSourceName());
    writer.indent();
    for (JMethod method : target.getMethods()) {
      if (method.getAnnotation(Subscribe.class) != null) {
        String paramType = getFirstParameterType(method);
        writer.println("if (event instanceof %s%s) {", paramType, getFilterPredicate(method));
        writer.indentln("owner.%s((%s) event);", method.getName(), paramType);
        writer.println("}");
      }
    }
    writer.outdent();
    writer.println("}");
  }

  private String getFilterPredicate(JMethod method) {
    StringBuilder predicate = new StringBuilder();
    When annotation = method.getAnnotation(When.class);
    if (annotation != null) {
      for (Class<?> filter : annotation.value()) {
        predicate.append(String.format("\n&& new %s().accepts((%s) event)",
            filter.getCanonicalName(), getFirstParameterType(method)));
      }
    }
    return predicate.toString();
  }

  private String getFirstParameterType(JMethod method) {
    // If the paramter type is primitive, box it
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
