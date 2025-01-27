package controllerlib.controller.method.parameters.adapters.base;


import controllerlib.exceptions.InvalidQueryParameterTypeException;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public abstract class AbstractQueryParameterAdapter<TAnnotation extends Annotation> extends ControllerMethodParameterAdapter<TAnnotation> {
    @Getter
    protected final String parameterName;

    public AbstractQueryParameterAdapter(Parameter parameter, TAnnotation annotation, String parameterName) {
        super(parameter, annotation);
        isParameterTypePrimitiveOrString(parameter);
        this.parameterName = parameterName;
    }

    private static void isParameterTypePrimitiveOrString(Parameter parameter) {
        if (!parameter.getType().isPrimitive() && !parameter.getType().equals(String.class)) {
            throw new InvalidQueryParameterTypeException(parameter.getName(), parameter.getType().getSimpleName());
        }
    }
}
