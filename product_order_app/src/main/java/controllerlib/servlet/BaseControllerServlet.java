package controllerlib.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllerlib.BaseController;
import controllerlib.ControllerResult;
import controllerlib.annotations.HttpDelete;
import controllerlib.annotations.HttpGet;
import controllerlib.annotations.HttpPost;
import controllerlib.annotations.HttpPut;
import controllerlib.exceptions.ControllerMethodParameterMappingException;
import controllerlib.servlet.parameters.ControllerMethodParameterValueMappersContainer;
import controllerlib.servlet.reflectioninfo.ControllerMethodInfo;
import controllerlib.servlet.reflectioninfo.ControllerMethodParameterInfo;
import controllerlib.servlet.reflectioninfo.ControllerMethodParameterType;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static controllerlib.servlet.reflectioninfo.ControllerMethodInfoCreator.buildControllerMethodInfos;

public abstract class BaseControllerServlet extends HttpServlet {
    private final Class<? extends BaseController> controllerClass = getControllerClass();

    private ControllerMethodInfo[] httpGetMethodInfos;
    private ControllerMethodInfo[] httpPostMethodInfos;
    private ControllerMethodInfo[] httpPutMethodInfos;
    private ControllerMethodInfo[] httpDeleteMethodInfos;

    private final ControllerMethodParameterValueMappersContainer parameterValueMappers = new ControllerMethodParameterValueMappersContainer();

    protected abstract Class<? extends BaseController> getControllerClass();

    @Override
    public void init() {
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

    private void processRequest(ControllerMethodInfo[] methodInfos, HttpServletRequest req, HttpServletResponse resp) {
        try {
            ControllerMethodInfo chosenMethodInfo = chooseMethod(methodInfos, req);

            if (chosenMethodInfo == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            BaseController controller = controllerClass.getConstructor().newInstance();
            // It is guaranteed that return type is ControllerResult by check in the init method
            var controllerResult = (ControllerResult) chosenMethodInfo.getMethod().invoke(controller, mapControllerMethodParameters(chosenMethodInfo, req));

            if (controllerResult.resultObject() != null) {
                String json = new ObjectMapper().writeValueAsString(controllerResult.resultObject());
                resp.getWriter().write(json);
            }

            resp.setStatus(controllerResult.statusCode());
        } catch (ControllerMethodParameterMappingException | JsonProcessingException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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
        for (var param : methodInfo.getParameterInfos()) {
            if (param.getControllerParameterType() == ControllerMethodParameterType.REQUIRED_QUERY_PARAM
                    && !requestParamMap.containsKey(param.getName())) {
                isSatisfied = false;
                break;
            }
        }
        return isSatisfied;
    }

    private Object[] mapControllerMethodParameters(ControllerMethodInfo methodInfo, HttpServletRequest request) throws ControllerMethodParameterMappingException {
        Object[] result = new Object[methodInfo.getParameterInfos().length];

        for (int i = 0; i < result.length; i++) {
            ControllerMethodParameterInfo parameterInfo = methodInfo.getParameterInfos()[i];
            result[i] = parameterValueMappers.mapValue(parameterInfo, request);
        }
        return result;
    }
}