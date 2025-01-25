package controllerlib.servlet.parameters.valuemappers;

import controllerlib.servlet.reflectioninfo.ControllerMethodParameterInfo;
import jakarta.servlet.http.HttpServletRequest;

import static controllerlib.servlet.TypeUtils.getDefaultValue;
import static controllerlib.servlet.TypeUtils.parsePrimitives;

public class NotRequiredQueryParamValueMapper implements ControllerMethodParameterValueMapper {
    @Override
    public Object mapValue(ControllerMethodParameterInfo parameterInfo, HttpServletRequest request) {
        var requestParamMap = request.getParameterMap();
        if (!requestParamMap.containsKey(parameterInfo.getName()) || requestParamMap.get(parameterInfo.getName()).length == 0) {
            return getDefaultValue(parameterInfo.getParameter().getType());
        }

        String requestParamValue = requestParamMap.get(parameterInfo.getName())[0];
        return parsePrimitives(parameterInfo.getParameter().getType(), requestParamValue);
    }
}
