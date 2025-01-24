package app.servlets;

import controllerlib.ControllerResult;
import controllerlib.annotations.*;
import app.controllers.VendorController;
import controllerlib.exceptions.InvalidControllerMethodReturnTypeException;
import controllerlib.exceptions.InvalidQueryParameterTypeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
class ControllerMethodInfo {
    Method method;
    ControllerMethodParameterInfo[] parameterInfos;
}

@Data
class ControllerMethodParameterInfo {
    Parameter parameter;
    String name = "";
    ControllerMethodParameterType controllerParameterType = ControllerMethodParameterType.NOT_ANNOTATED;
}

enum ControllerMethodParameterType {
    NOT_ANNOTATED,
    REQUIRED_QUERY_PARAM,
    NOT_REQUIRED_QUERY_PARAM,
}

@WebServlet("/vendor")
public class VendorServlet extends HttpServlet {
    //    private static final Class[] REGISTERED_CONTROLLER_METHOD_PARAMETER_ANNOTATIONS = new Class[]{NotRequiredQueryParam.class, RequiredQueryParam.class};
    private final Class<VendorController> controllerClass = VendorController.class;

    private ControllerMethodInfo[] httpGetMethodInfos;
    private ControllerMethodInfo[] httpPostMethodInfos;
    private ControllerMethodInfo[] httpPutMethodInfos;
    private ControllerMethodInfo[] httpDeleteMethodInfos;

    @Override
    public void init() throws ServletException {
        httpGetMethodInfos = buildControllerMethodInfos(HttpGet.class);
        httpPostMethodInfos = buildControllerMethodInfos(HttpPost.class);
        httpPutMethodInfos = buildControllerMethodInfos(HttpPut.class);
        httpDeleteMethodInfos = buildControllerMethodInfos(HttpDelete.class);
    }

    private ControllerMethodInfo[] buildControllerMethodInfos(Class<? extends Annotation> httpAnnotation) {
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpGetMethodInfos, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpPostMethodInfos, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpPutMethodInfos, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpDeleteMethodInfos, req, resp);
    }

    @SneakyThrows
    private void processRequest(ControllerMethodInfo[] methodInfos, HttpServletRequest req, HttpServletResponse resp) {
        ControllerMethodInfo chosenMethodInfo = chooseMethod(methodInfos, req);

        if (chosenMethodInfo == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Object controller = controllerClass.getConstructor().newInstance();
        Object methodInvocationResult = chosenMethodInfo.getMethod().invoke(controller, mapControllerMethodParameters(chosenMethodInfo, req.getParameterMap()));
        // It is guaranteed that return type is ControllerResult by check in the init method
        var controllerResult = (ControllerResult) methodInvocationResult;

        if (controllerResult.statusCode() == HttpServletResponse.SC_OK) {
            String json = new ObjectMapper().writeValueAsString(controllerResult.resultObject());
            resp.getWriter().write(json);
        }
        resp.setStatus(controllerResult.statusCode());
    }

    private ControllerMethodInfo chooseMethod(ControllerMethodInfo[] methodInfos, HttpServletRequest req) {
        ControllerMethodInfo chosenMethodInfo = null;
        for (var methodInfo : methodInfos) {
            if (isMethodSatisfiedByRequiredQueryParams(methodInfo, req.getParameterMap())) {
                chosenMethodInfo = methodInfo;
                break;
            }
        }
        return chosenMethodInfo;
    }

    private static boolean isMethodSatisfiedByRequiredQueryParams(ControllerMethodInfo methodInfo, Map<String, String[]> requestParamMap) {
        boolean isSatisfied = true;
        for (var param : methodInfo.parameterInfos) {
            if (param.controllerParameterType == ControllerMethodParameterType.REQUIRED_QUERY_PARAM
                    && !requestParamMap.containsKey(param.name)) {
                isSatisfied = false;
                break;
            }
        }
        return isSatisfied;
    }

    private static int countRequiredQueryParamsOfMethod(Method method) {
        return getRequiredQueryParamsOfMethod(method).size();
    }

    private static List<Parameter> getRequiredQueryParamsOfMethod(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(param -> param.isAnnotationPresent(RequiredQueryParam.class))
                .toList();
    }

    private static Object[] mapControllerMethodParameters(ControllerMethodInfo methodInfo, Map<String, String[]> requestParamMap) {
        Object[] result = new Object[methodInfo.parameterInfos.length];

        for (int i = 0; i < result.length; i++) {
            ControllerMethodParameterInfo parameterInfo = methodInfo.parameterInfos[i];

            if (parameterInfo.controllerParameterType == ControllerMethodParameterType.REQUIRED_QUERY_PARAM) {
                String requestParamValue = requestParamMap.get(parameterInfo.name)[0];
                result[i] = parsePrimitives(parameterInfo.parameter.getType(), requestParamValue);
            } else if (parameterInfo.controllerParameterType == ControllerMethodParameterType.NOT_REQUIRED_QUERY_PARAM) {
                if (!requestParamMap.containsKey(parameterInfo.name) || requestParamMap.get(parameterInfo.name).length == 0) {
                    result[i] = getDefaultValue(parameterInfo.parameter.getType());
                    continue;
                }

                String requestParamValue = requestParamMap.get(parameterInfo.name)[0];
                result[i] = parsePrimitives(parameterInfo.parameter.getType(), requestParamValue);
            } else {
                result[i] = getDefaultValue(parameterInfo.parameter.getType());
            }
        }
        return result;
    }

    private static Object getDefaultValue(Class<?> type) {
        if (type.equals(int.class)) {
            return 0;
        } else if (type.equals(boolean.class)) {
            return false;
        } else if (type.equals(char.class)) {
            return 0;
        } else if (type.equals(double.class)) {
            return 0.0;
        } else if (type.equals(float.class)) {
            return 0.0f;
        } else if (type.equals(long.class)) {
            return 0L;
        } else if (type.equals(short.class)) {
            return (short) 0;
        } else if (type.equals(byte.class)) {
            return (byte) 0;
        } else {
            return null;
        }
    }

    private static Object parsePrimitives(Class type, String stringValue) {
        if (type.equals(String.class)) {
            return stringValue;
        } else if (type.equals(int.class)) {
            return Integer.parseInt(stringValue);
        } else if (type.equals(boolean.class)) {
            return Boolean.parseBoolean(stringValue);
        } else if (type.equals(char.class)) {
            return stringValue.charAt(0);
        } else if (type.equals(double.class)) {
            return Double.parseDouble(stringValue);
        } else if (type.equals(float.class)) {
            return Float.parseFloat(stringValue);
        } else if (type.equals(long.class)) {
            return Long.parseLong(stringValue);
        } else if (type.equals(short.class)) {
            return Short.parseShort(stringValue);
        } else {
            return Byte.parseByte(stringValue);
        }
    }
}
