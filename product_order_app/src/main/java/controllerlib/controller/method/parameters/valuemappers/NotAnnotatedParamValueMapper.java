package controllerlib.controller.method.parameters.valuemappers;

import controllerlib.exceptions.ControllerMethodParameterMappingException;
import controllerlib.controller.method.reflectioninfo.ControllerMethodParameterInfo;
import jakarta.servlet.http.HttpServletRequest;

import static controllerlib.TypeUtils.getDefaultValue;

public class NotAnnotatedParamValueMapper implements ControllerMethodParameterValueMapper {
    @Override
    public Object mapValue(ControllerMethodParameterInfo parameterInfo, HttpServletRequest request) throws ControllerMethodParameterMappingException {
        return getDefaultValue(parameterInfo.getParameter().getType());
    }
}
