package controllerlib.servlet;

import controllerlib.BaseController;
import controllerlib.ControllerResult;
import controllerlib.annotations.FromRequestBody;
import controllerlib.annotations.NotRequiredQueryParam;
import controllerlib.annotations.RequiredQueryParam;
import controllerlib.exceptions.InvalidControllerMethodReturnTypeException;
import controllerlib.exceptions.InvalidQueryParameterTypeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

class ControllerMethodInfoCreator {
    public static ControllerMethodInfo[] buildControllerMethodInfos(Class<? extends BaseController> controllerClass, Class<? extends Annotation> httpAnnotation) {
        List<Method> methods = Arrays.stream(controllerClass.getMethods())
                .filter(method -> method.isAnnotationPresent(httpAnnotation))
                .sorted((o1, o2) -> {
                    int first = countRequiredQueryParamsOfMethod(o1);
                    int second = countRequiredQueryParamsOfMethod(o2);
                    return Integer.compare(second, first);
                }).toList();

        ControllerMethodInfo[] controllerMethodInfos = new ControllerMethodInfo[methods.size()];

        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (!method.getReturnType().equals(ControllerResult.class)) {
                throw new InvalidControllerMethodReturnTypeException(method.getName());
            }
            controllerMethodInfos[i] = getControllerMethodInfo(method);
        }

        return controllerMethodInfos;
    }

    private static ControllerMethodInfo getControllerMethodInfo(Method method) {
        var info = new ControllerMethodInfo();
        info.method = method;
        Parameter[] parameters = method.getParameters();
        info.parameterInfos = new ControllerMethodParameterInfo[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            info.parameterInfos[i] = getControllerMethodParameterInfo(parameters[i]);
        }
        return info;
    }

    private static ControllerMethodParameterInfo getControllerMethodParameterInfo(Parameter parameter) {
        var info = new ControllerMethodParameterInfo();
        info.parameter = parameter;
        if (parameter.isAnnotationPresent(RequiredQueryParam.class)) {
            checkQueryParameterType(parameter);
            info.controllerParameterType = ControllerMethodParameterType.REQUIRED_QUERY_PARAM;
            info.name = parameter.getAnnotationsByType(RequiredQueryParam.class)[0].value();
        } else if (parameter.isAnnotationPresent(NotRequiredQueryParam.class)) {
            checkQueryParameterType(parameter);
            info.controllerParameterType = ControllerMethodParameterType.NOT_REQUIRED_QUERY_PARAM;
            info.name = parameter.getAnnotationsByType(NotRequiredQueryParam.class)[0].value();
        } else if (parameter.isAnnotationPresent(FromRequestBody.class)) {
            info.controllerParameterType = ControllerMethodParameterType.FROM_REQUEST_BODY;
        } else {
            info.controllerParameterType = ControllerMethodParameterType.NOT_ANNOTATED;
        }
        return info;
    }

    private static void checkQueryParameterType(Parameter parameter) {
        if(!parameter.getType().isPrimitive() && !parameter.getType().equals(String.class)) {
            throw new InvalidQueryParameterTypeException(parameter.getName(), parameter.getType().getSimpleName());
        }
    }

    private static int countRequiredQueryParamsOfMethod(Method method) {
        return getRequiredQueryParamsOfMethod(method).size();
    }

    private static List<Parameter> getRequiredQueryParamsOfMethod(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(param -> param.isAnnotationPresent(RequiredQueryParam.class))
                .toList();
    }
}
