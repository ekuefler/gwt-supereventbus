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

import com.ekuefler.supereventbus.shared.impl.EventHandlerMethod;
import com.ekuefler.supereventbus.shared.multievent.MultiEvent;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Generator for {@link com.ekuefler.supereventbus.shared.EventRegistration}. Takes care of the ugly
 * parts of creating the source writer and then delegates to {@link EventRegistrationWriter}. This
 * class is used by the GWT compiler and should not be referenced directly by users.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 */
public class EventRegistrationGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    try {
      JClassType eventBinderType = context.getTypeOracle().getType(typeName);
      JClassType targetType = getTargetType(eventBinderType, context.getTypeOracle());
      SourceWriter writer = createSourceWriter(logger, context, eventBinderType, targetType);
      if (writer != null) { // Otherwise the class was already created
        new EventRegistrationWriter(logger).writeGetMethods(targetType, writer);
        writer.commit(logger);
      }
      return new StringBuilder()
          .append(eventBinderType.getPackage().getName())
          .append('.')
          .append(getSimpleGeneratedClassName(eventBinderType))
          .toString();
    } catch (NotFoundException e) {
      logger.log(Type.ERROR, "Error generating " + typeName, e);
      throw new UnableToCompleteException();
    }
  }

  private JClassType getTargetType(JClassType interfaceType, TypeOracle typeOracle) {
    JClassType[] superTypes = interfaceType.getImplementedInterfaces();
    return superTypes[0].isParameterized().getTypeArgs()[0];
  }

  private SourceWriter createSourceWriter(TreeLogger logger, GeneratorContext context,
      JClassType eventBinderType, JClassType targetType) {
    String simpleName = getSimpleGeneratedClassName(eventBinderType);
    String packageName = eventBinderType.getPackage().getName();
    ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
        packageName, simpleName);

    composer.addImplementedInterface(eventBinderType.getName());
    composer.addImport(EventHandlerMethod.class.getCanonicalName());
    composer.addImport(LinkedList.class.getCanonicalName());
    composer.addImport(List.class.getCanonicalName());
    composer.addImport(MultiEvent.class.getCanonicalName());

    PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
    return printWriter != null ? composer.createSourceWriter(context, printWriter) : null;
  }

  private String getSimpleGeneratedClassName(JClassType eventBinderType) {
    return eventBinderType.getName().replace('.', '_') + "Impl";
  }
}
