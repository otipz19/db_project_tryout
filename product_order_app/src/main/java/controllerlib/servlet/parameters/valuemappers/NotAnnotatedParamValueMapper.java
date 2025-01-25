package controllerlib.servlet.parameters.valuemappers;

import controllerlib.exceptions.ControllerMethodParameterMappingException;
import controllerlib.servlet.reflectioninfo.ControllerMethodParameterInfo;
import jakarta.servlet.http.HttpServletRequest;

import static controllerlib.servlet.TypeUtils.getDefaultValue;

public class NotAnnotatedParamValueMapper implements ControllerMethodParameterValueMapper {
    @Override
    public Object mapValue(ControllerMethodParameterInfo parameterInfo, HttpServletRequest request) throws ControllerMethodParameterMappingException {
        return getDefaultValue(parameterInfo.getParameter().getType());
    }
}
