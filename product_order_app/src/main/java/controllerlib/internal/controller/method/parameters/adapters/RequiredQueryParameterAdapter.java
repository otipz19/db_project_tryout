package controllerlib.internal.controller.method.parameters.adapters;

import controllerlib.exposed.annotations.RequiredQueryParam;
import controllerlib.internal.controller.method.parameters.adapters.base.AbstractQueryParameterAdapter;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Parameter;

import static controllerlib.internal.TypeUtils.parsePrimitives;

public class RequiredQueryParameterAdapter extends AbstractQueryParameterAdapter<RequiredQueryParam> {
    public RequiredQueryParameterAdapter(Parameter parameter, RequiredQueryParam annotation) {
        super(parameter, annotation, annotation.value());
    }

    @Override
    public Object mapValue(HttpServletRequest request) {
        String requestParamValue = request.getParameterMap().get(parameterName)[0];
        return parsePrimitives(parameter.getType(), requestParamValue);
    }
}
