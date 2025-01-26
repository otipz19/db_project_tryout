package controllerlib.controller.method;

import controllerlib.annotations.FromRequestBody;
import controllerlib.annotations.NotRequiredQueryParam;
import controllerlib.annotations.RequiredQueryParam;
import controllerlib.controller.BaseController;
import controllerlib.controller.ControllerResult;
import controllerlib.controller.method.parameters.ControllerMethodParameterValueMappersContainer;
import controllerlib.controller.method.reflectioninfo.ControllerMethodParameterInfo;
import controllerlib.controller.method.reflectioninfo.ControllerMethodParameterType;
import controllerlib.exceptions.ControllerMethodParameterMappingException;
import controllerlib.exceptions.InvalidQueryParameterTypeException;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ControllerMethodAdapter implements Comparable<ControllerMethodAdapter> {
    // TODO: Combine ParameterInfo and value mappers
    private static final ControllerMethodParameterValueMappersContainer parameterValueMappers = new ControllerMethodParameterValueMappersContainer();

    private final Class<? extends BaseController> controller;
    private final Method method;
    private ControllerMethodParameterInfo[] parameterInfos;
    private final List<ControllerMethodParameterInfo> requiredParameterInfos = new LinkedList<>();

    public ControllerMethodAdapter(Class<? extends BaseController> controller, Method method) {
        this.controller = controller;
        this.method = method;
        createParameterInfos();
    }

    private ControllerMethodParameterInfo[] createParameterInfos() {
        Parameter[] parameters = method.getParameters();
        this.parameterInfos = new ControllerMethodParameterInfo[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            var parameterInfo = createParameterInfo(parameters[i]);
            parameterInfos[i] = parameterInfo;
            if(parameterInfo.getControllerParameterType() == ControllerMethodParameterType.REQUIRED_QUERY_PARAM) {
                requiredParameterInfos.add(parameterInfo);
            }
        }
        return parameterInfos;
    }

    private ControllerMethodParameterInfo createParameterInfo(Parameter parameter) {
        var info = new ControllerMethodParameterInfo();
        info.setParameter(parameter);
        if (parameter.isAnnotationPresent(RequiredQueryParam.class)) {
            checkQueryParameterType(parameter);
            info.setControllerParameterType(ControllerMethodParameterType.REQUIRED_QUERY_PARAM);
            var annotation = parameter.getAnnotationsByType(RequiredQueryParam.class)[0];
            info.setAnnotation(annotation);
            info.setName(annotation.value());
        } else if (parameter.isAnnotationPresent(NotRequiredQueryParam.class)) {
            checkQueryParameterType(parameter);
            info.setControllerParameterType(ControllerMethodParameterType.NOT_REQUIRED_QUERY_PARAM);
            var annotation = parameter.getAnnotationsByType(NotRequiredQueryParam.class)[0];
            info.setAnnotation(annotation);
            info.setName(annotation.value());
        } else if (parameter.isAnnotationPresent(FromRequestBody.class)) {
            info.setControllerParameterType(ControllerMethodParameterType.FROM_REQUEST_BODY);
            info.setAnnotation(parameter.getAnnotationsByType(FromRequestBody.class)[0]);
        } else {
            info.setControllerParameterType(ControllerMethodParameterType.NOT_ANNOTATED);
        }
        return info;
    }

    private static void checkQueryParameterType(Parameter parameter) {
        if (!parameter.getType().isPrimitive() && !parameter.getType().equals(String.class)) {
            throw new InvalidQueryParameterTypeException(parameter.getName(), parameter.getType().getSimpleName());
        }
    }

    public boolean isMethodSatisfiedByRequiredQueryParams(Map<String, String[]> requestParamMap) {
        for (var param : this.requiredParameterInfos) {
            if (!requestParamMap.containsKey(param.getName())) {
                return false;
            }
        }
        return true;
    }

    public ControllerResult<?> invoke(HttpServletRequest request) throws ControllerMethodParameterMappingException {
        try {
            BaseController controller = this.controller.getConstructor().newInstance();
            // It is guaranteed that return type is ControllerResult by check in the init method
            return (ControllerResult<?>) method.invoke(controller, mapControllerMethodParameters(request));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] mapControllerMethodParameters(HttpServletRequest request) throws ControllerMethodParameterMappingException {
        Object[] result = new Object[this.parameterInfos.length];

        for (int i = 0; i < result.length; i++) {
            ControllerMethodParameterInfo parameterInfo = this.parameterInfos[i];
            result[i] = parameterValueMappers.mapValue(parameterInfo, request);
        }
        return result;
    }

    @Override
    public int compareTo(ControllerMethodAdapter other) {
        // reversed intentionally
        return Integer.compare(other.requiredParameterInfos.size(), this.requiredParameterInfos.size());
    }
}
