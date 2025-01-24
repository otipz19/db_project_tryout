package app.servlets;

import controllerlib.ControllerResult;
import controllerlib.annotations.GetMethod;
import controllerlib.annotations.NotRequiredQueryParam;
import controllerlib.annotations.RequiredQueryParam;
import app.controllers.VendorController;
import controllerlib.exceptions.InvalidControllerMethodReturnTypeException;
import controllerlib.exceptions.InvalidQueryParameterTypeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@WebServlet("/vendor")
public class VendorServlet extends HttpServlet {
    private static final Class<VendorController> controllerClass = VendorController.class;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Object controller = controllerClass.getConstructor().newInstance();

            List<Method> getMethods = Arrays.stream(controllerClass.getMethods())
                    .filter(method -> method.isAnnotationPresent(GetMethod.class))
                    .sorted((o1, o2) -> {
                        int first = countRequiredQueryParamsOfMethod(o1);
                        int second = countRequiredQueryParamsOfMethod(o2);
                        return Integer.compare(second, first);
                    }).toList();

            Method chosenMethod = null;
            for (var method : getMethods) {
                if (isMethodSatisfiedByRequiredQueryParams(method, req.getParameterMap())) {
                    chosenMethod = method;
                    break;
                }
            }

            if (chosenMethod == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Object methodInvocationResult = chosenMethod.invoke(controller, getControllerMethodParameters(chosenMethod, req.getParameterMap()));
            if (!methodInvocationResult.getClass().equals(ControllerResult.class)) {
                throw new InvalidControllerMethodReturnTypeException(chosenMethod.getName());
            }
            var controllerResult = (ControllerResult) methodInvocationResult;

            if (controllerResult.statusCode() == HttpServletResponse.SC_OK) {
                String json = new ObjectMapper().writeValueAsString(controllerResult.resultObject());
                resp.getWriter().write(json);
            }
            resp.setStatus(controllerResult.statusCode());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static int countRequiredQueryParamsOfMethod(Method method) {
        return getRequiredQueryParamsOfMethod(method).size();
    }

    private static boolean isMethodSatisfiedByRequiredQueryParams(Method method, Map<String, String[]> requestParamMap) {
        List<Parameter> requiredParams = getRequiredQueryParamsOfMethod(method);
        boolean isSatisfied = true;
        for (var param : requiredParams) {
            if (!requestParamMap.containsKey(param.getAnnotationsByType(RequiredQueryParam.class)[0].value())) {
                isSatisfied = false;
                break;
            }
        }
        return isSatisfied;
    }

    private static List<Parameter> getRequiredQueryParamsOfMethod(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(param -> param.isAnnotationPresent(RequiredQueryParam.class))
                .toList();
    }

    private static Object[] getControllerMethodParameters(Method method, Map<String, String[]> requestParamMap) {
        List<Parameter> parameters = Arrays.stream(method.getParameters()).toList();
        Object[] result = new Object[parameters.size()];

        for (int i = 0; i < result.length; i++) {
            Parameter parameter = parameters.get(i);
            if (parameter.isAnnotationPresent(RequiredQueryParam.class)) {
                String paramName = parameter.getAnnotationsByType(RequiredQueryParam.class)[0].value();
                String requestParamValue = requestParamMap.get(paramName)[0];
                result[i] = parseQueryParamValue(parameter, requestParamValue);
            } else if (parameter.isAnnotationPresent(NotRequiredQueryParam.class)) {
                String paramName = parameter.getAnnotationsByType(NotRequiredQueryParam.class)[0].value();
                if (!requestParamMap.containsKey(paramName) || requestParamMap.get(paramName).length == 0) {
                    result[i] = getDefaultValue(parameter.getType());
                    continue;
                }
                String requestParamValue = requestParamMap.get(paramName)[0];
                result[i] = parseQueryParamValue(parameter, requestParamValue);
            } else {
                result[i] = getDefaultValue(parameter.getType());
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

    private static Object parseQueryParamValue(Parameter parameter, String stringValue) {
        Class parameterType = parameter.getType();
        if (parameterType.equals(String.class)) {
            return stringValue;
        } else if (parameterType.equals(int.class)) {
            return Integer.parseInt(stringValue);
        } else if (parameterType.equals(boolean.class)) {
            return Boolean.parseBoolean(stringValue);
        } else if (parameterType.equals(char.class)) {
            return stringValue.charAt(0);
        } else if (parameterType.equals(double.class)) {
            return Double.parseDouble(stringValue);
        } else if (parameterType.equals(float.class)) {
            return Float.parseFloat(stringValue);
        } else if (parameterType.equals(long.class)) {
            return Long.parseLong(stringValue);
        } else if (parameterType.equals(short.class)) {
            return Short.parseShort(stringValue);
        } else if (parameterType.equals(byte.class)) {
            return Byte.parseByte(stringValue);
        } else {
            throw new InvalidQueryParameterTypeException(parameter.getName(), parameterType.getSimpleName());
        }
    }
}
