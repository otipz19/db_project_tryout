package controllerlib.controller.method.parameters.adapters.base;

import controllerlib.exceptions.ControllerMethodParameterMappingException;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public abstract class ControllerMethodParameterAdapter<TAnnotation extends Annotation> {
    protected final Parameter parameter;
    protected final TAnnotation annotation;

    public ControllerMethodParameterAdapter(Parameter parameter, TAnnotation annotation) {
        this.parameter = parameter;
        this.annotation = annotation;
    }

    /**
     * This method have to map adapted parameter to its value with which the method will be called
     * @return actual value of adapted parameter
     * @throws ControllerMethodParameterMappingException at mapping fail, API should return 403 Bad Request
     */
    public abstract Object mapValue(HttpServletRequest request) throws ControllerMethodParameterMappingException;
}
