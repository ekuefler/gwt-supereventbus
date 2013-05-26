package com.ekuefler.supereventbus.gwt.rebind;

import com.ekuefler.supereventbus.shared.Subscribe;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;

class EventRegistrationWriter {
  void writeDispatch(JClassType target, SourceWriter writer) {
    writer.println("public void dispatch(Object owner, Object event) {");
    writer.indent();
    for (JMethod method : target.getMethods()) {
      if (method.getAnnotation(Subscribe.class) != null) {
        String paramType = getFirstParameterType(method);
        writer.print("if (event instanceof %s) {", paramType);
        writer.indentln("((%s) owner).%s((%s) event);",
            target.getQualifiedSourceName(), method.getName(), paramType);
        writer.print("}");
      }
    }
    writer.outdent();
    writer.println("}");
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
