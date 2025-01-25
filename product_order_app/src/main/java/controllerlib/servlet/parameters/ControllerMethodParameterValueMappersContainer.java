package controllerlib.servlet.parameters;

import controllerlib.exceptions.ControllerMethodParameterMappingException;
import controllerlib.servlet.parameters.valuemappers.*;
import controllerlib.servlet.reflectioninfo.ControllerMethodParameterInfo;
import controllerlib.servlet.reflectioninfo.ControllerMethodParameterType;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ControllerMethodParameterValueMappersContainer implements ControllerMethodParameterValueMapper {
    private final HashMap<ControllerMethodParameterType, ControllerMethodParameterValueMapper> mappers = new HashMap<>();

    public ControllerMethodParameterValueMappersContainer() {
        register(ControllerMethodParameterType.REQUIRED_QUERY_PARAM, RequiredQueryParamValueMapper.class);
        register(ControllerMethodParameterType.NOT_REQUIRED_QUERY_PARAM, NotRequiredQueryParamValueMapper.class);
        register(ControllerMethodParameterType.FROM_REQUEST_BODY, RequestBodyParamValueMapper.class);
        register(ControllerMethodParameterType.NOT_ANNOTATED, NotAnnotatedParamValueMapper.class);
    }

    public void register(ControllerMethodParameterType type, Class<? extends ControllerMethodParameterValueMapper> mapper) {
        try {
            mappers.put(type, mapper.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Controller method parameter value mapper must have default constructor");
        }
    }

    @Override
    public Object mapValue(ControllerMethodParameterInfo parameterInfo, HttpServletRequest request) throws ControllerMethodParameterMappingException {
        return mappers.get(parameterInfo.getControllerParameterType()).mapValue(parameterInfo, request);
    }
}
