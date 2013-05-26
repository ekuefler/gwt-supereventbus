package com.ekuefler.supereventbus.gwt.rebind;

import com.ekuefler.supereventbus.shared.Subscribe;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.user.rebind.SourceWriter;

class EventRegistrationWriter {
  void writeDispatch(JClassType target, SourceWriter writer) {
    writer.println("public void dispatch(Object owner, Object event) {");
    writer.indent();
    for (JMethod method : target.getMethods()) {
      if (method.getAnnotation(Subscribe.class) != null) {
        String paramType = method.getParameterTypes()[0].getQualifiedSourceName();
        writer.print("if (event instanceof %s) {", paramType);
        writer.indentln("((%s) owner).%s((%s) event);",
            target.getQualifiedSourceName(), method.getName(), paramType);
        writer.print("}");
      }
    }
    writer.outdent();
    writer.println("}");
  }
}
