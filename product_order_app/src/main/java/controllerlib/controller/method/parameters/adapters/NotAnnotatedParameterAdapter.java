package controllerlib.controller.method.parameters.adapters;

import controllerlib.controller.method.parameters.adapters.base.ControllerMethodParameterAdapter;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import static controllerlib.TypeUtils.getDefaultValue;

public class NotAnnotatedParameterAdapter extends ControllerMethodParameterAdapter<Annotation> {
    public NotAnnotatedParameterAdapter(Parameter parameter) {
        super(parameter, null);
    }

    @Override
    public Object mapValue(HttpServletRequest request) {
        return getDefaultValue(parameter.getType());
    }
}
