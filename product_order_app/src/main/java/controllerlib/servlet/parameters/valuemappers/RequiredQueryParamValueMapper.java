package controllerlib.servlet.parameters.valuemappers;

import controllerlib.servlet.reflectioninfo.ControllerMethodParameterInfo;
import jakarta.servlet.http.HttpServletRequest;

import static controllerlib.servlet.TypeUtils.parsePrimitives;

public class RequiredQueryParamValueMapper implements ControllerMethodParameterValueMapper {
    @Override
    public Object mapValue(ControllerMethodParameterInfo parameterInfo, HttpServletRequest request) {
        String requestParamValue = request.getParameterMap().get(parameterInfo.getName())[0];
        return parsePrimitives(parameterInfo.getParameter().getType(), requestParamValue);
    }
}
