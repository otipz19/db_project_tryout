package controllerlib.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllerlib.annotations.HttpDelete;
import controllerlib.annotations.HttpGet;
import controllerlib.annotations.HttpPost;
import controllerlib.annotations.HttpPut;
import controllerlib.controller.BaseController;
import controllerlib.controller.ControllerResult;
import controllerlib.controller.method.ControllerMethodAdapter;
import controllerlib.exceptions.ControllerMethodParameterMappingException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static controllerlib.controller.method.reflectioninfo.ControllerMethodAdapterCreator.buildControllerMethodAdapters;

public abstract class BaseControllerServlet extends HttpServlet {
    private final Class<? extends BaseController> controllerClass = getControllerClass();

    private ControllerMethodAdapter[] httpGetMethodAdapters;
    private ControllerMethodAdapter[] httpPostMethodAdapters;
    private ControllerMethodAdapter[] httpPutMethodAdapters;
    private ControllerMethodAdapter[] httpDeleteMethodAdapters;

    protected abstract Class<? extends BaseController> getControllerClass();

    @Override
    public void init() {
        httpGetMethodAdapters = buildControllerMethodAdapters(controllerClass, HttpGet.class);
        httpPostMethodAdapters = buildControllerMethodAdapters(controllerClass, HttpPost.class);
        httpPutMethodAdapters = buildControllerMethodAdapters(controllerClass, HttpPut.class);
        httpDeleteMethodAdapters = buildControllerMethodAdapters(controllerClass, HttpDelete.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpGetMethodAdapters, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpPostMethodAdapters, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpPutMethodAdapters, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(this.httpDeleteMethodAdapters, req, resp);
    }

    private void processRequest(ControllerMethodAdapter[] methodAdapters, HttpServletRequest req, HttpServletResponse resp) {
        try {
            ControllerMethodAdapter chosenMethodAdapter = chooseMethodAdapter(methodAdapters, req);

            if (chosenMethodAdapter == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            ControllerResult<?> controllerResult = chosenMethodAdapter.invoke(req);

            resp.setStatus(controllerResult.statusCode());
            if (controllerResult.resultObject() != null) {
                String json = new ObjectMapper().writeValueAsString(controllerResult.resultObject());
                resp.getWriter().write(json);
            }
        } catch (ControllerMethodParameterMappingException | JsonProcessingException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private ControllerMethodAdapter chooseMethodAdapter(ControllerMethodAdapter[] methodAdapters, HttpServletRequest req) {
        for (var methodAdapter : methodAdapters) {
            if (methodAdapter.isMethodSatisfiedByRequiredQueryParams(req.getParameterMap())) {
                return methodAdapter;
            }
        }
        return null;
    }
}