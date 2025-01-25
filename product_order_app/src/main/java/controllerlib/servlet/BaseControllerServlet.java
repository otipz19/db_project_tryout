package controllerlib.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllerlib.BaseController;
import controllerlib.ControllerResult;
import controllerlib.annotations.HttpDelete;
import controllerlib.annotations.HttpGet;
import controllerlib.annotations.HttpPost;
import controllerlib.annotations.HttpPut;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.util.Map;

import static controllerlib.servlet.ControllerMethodInfoCreator.buildControllerMethodInfos;
import static controllerlib.servlet.TypeUtils.getDefaultValue;
import static controllerlib.servlet.TypeUtils.parsePrimitives;

public abstract class BaseControllerServlet extends HttpServlet {
    private final Class<? extends BaseController> controllerClass = getControllerClass();
    protected abstract Class<? extends BaseController> getControllerClass();

    private ControllerMethodInfo[] httpGetMethodInfos;
    private ControllerMethodInfo[] httpPostMethodInfos;
    private ControllerMethodInfo[] httpPutMethodInfos;
    private ControllerMethodInfo[] httpDeleteMethodInfos;

    @Override
    public void init() throws ServletException {
        httpGetMethodInfos = buildControllerMethodInfos(controllerClass, HttpGet.class);
        httpPostMethodInfos = buildControllerMethodInfos(controllerClass, HttpPost.class);
        httpPutMethodInfos = buildControllerMethodInfos(controllerClass, HttpPut.class);
        httpDeleteMethodInfos = buildControllerMethodInfos(controllerClass, HttpDelete.class);
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
}