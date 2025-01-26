package controllerlib.controller.method.parameters.valuemappers;

import controllerlib.exceptions.ControllerMethodParameterMappingException;
import controllerlib.controller.method.reflectioninfo.ControllerMethodParameterInfo;
import jakarta.servlet.http.HttpServletRequest;

public interface ControllerMethodParameterValueMapper {
    Object mapValue(ControllerMethodParameterInfo parameterInfo, HttpServletRequest request) throws ControllerMethodParameterMappingException;
}
