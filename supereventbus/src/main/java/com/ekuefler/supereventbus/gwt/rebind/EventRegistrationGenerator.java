package com.ekuefler.supereventbus.gwt.rebind;

import java.io.PrintWriter;

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

public class EventRegistrationGenerator extends Generator {

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    try {
      JClassType eventBinderType = context.getTypeOracle().getType(typeName);
      JClassType targetType = getTargetType(eventBinderType, context.getTypeOracle());
      SourceWriter writer = createSourceWriter(logger, context, eventBinderType, targetType);
      if (writer != null) { // Otherwise the class was already created
        new EventRegistrationWriter().writeDispatch(targetType, writer);
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

    PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
    return printWriter != null ? composer.createSourceWriter(context, printWriter) : null;
  }

  private String getSimpleGeneratedClassName(JClassType eventBinderType) {
    return eventBinderType.getName().replace('.', '_') + "Impl";
  }
}
