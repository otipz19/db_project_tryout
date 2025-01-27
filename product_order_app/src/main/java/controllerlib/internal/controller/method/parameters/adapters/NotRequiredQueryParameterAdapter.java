package controllerlib.internal.controller.method.parameters.adapters;

import controllerlib.exposed.annotations.NotRequiredQueryParam;
import controllerlib.internal.controller.method.parameters.adapters.base.AbstractQueryParameterAdapter;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Parameter;

import static controllerlib.internal.TypeUtils.getDefaultValue;
import static controllerlib.internal.TypeUtils.parsePrimitives;

public class NotRequiredQueryParameterAdapter extends AbstractQueryParameterAdapter<NotRequiredQueryParam> {
    public NotRequiredQueryParameterAdapter(Parameter parameter, NotRequiredQueryParam annotation) {
        super(parameter, annotation, annotation.value());
    }

    @Override
    public Object mapValue(HttpServletRequest request) {
        var requestParamMap = request.getParameterMap();
        if (!requestParamMap.containsKey(parameterName) || requestParamMap.get(parameterName).length == 0) {
            return getDefaultValue(parameter.getType());
        }

        String requestParamValue = requestParamMap.get(parameterName)[0];
        return parsePrimitives(parameter.getType(), requestParamValue);
    }
}
