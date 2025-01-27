package controllerlib.controller.method.adapters;

import controllerlib.annotations.FromRequestBody;
import controllerlib.annotations.NotRequiredQueryParam;
import controllerlib.annotations.RequiredQueryParam;
import controllerlib.controller.BaseController;
import controllerlib.controller.ControllerResult;
import controllerlib.controller.method.parameters.adapters.NotAnnotatedParameterAdapter;
import controllerlib.controller.method.parameters.adapters.NotRequiredQueryParameterAdapter;
import controllerlib.controller.method.parameters.adapters.RequestBodyParameterAdapter;
import controllerlib.controller.method.parameters.adapters.RequiredQueryParameterAdapter;
import controllerlib.controller.method.parameters.adapters.base.AbstractQueryParameterAdapter;
import controllerlib.controller.method.parameters.adapters.base.ControllerMethodParameterAdapter;
import controllerlib.exceptions.ControllerMethodParameterMappingException;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ControllerMethodAdapter implements Comparable<ControllerMethodAdapter> {
    private final Class<? extends BaseController> controller;
    private final Method method;
    private ControllerMethodParameterAdapter<?>[] parameterAdapters;
    private final List<AbstractQueryParameterAdapter<RequiredQueryParam>> requiredParameterAdapters = new LinkedList<>();

    public ControllerMethodAdapter(Class<? extends BaseController> controller, Method method) {
        this.controller = controller;
        this.method = method;
        createParameterAdapters();
    }

    private void createParameterAdapters() {
        Parameter[] parameters = method.getParameters();
        this.parameterAdapters = new ControllerMethodParameterAdapter<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterAdapters[i] = createParameterAdapter(parameters[i]);
        }
    }

    private ControllerMethodParameterAdapter<?> createParameterAdapter(Parameter parameter) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation instanceof RequiredQueryParam requiredQueryParam) {
                var adapter = new RequiredQueryParameterAdapter(parameter, requiredQueryParam);
                requiredParameterAdapters.add(adapter);
                return adapter;
            }

            if(annotation instanceof NotRequiredQueryParam notRequiredQueryParam) {
                return new NotRequiredQueryParameterAdapter(parameter, notRequiredQueryParam);
            }

            if(annotation instanceof FromRequestBody fromRequestBody) {
                return new RequestBodyParameterAdapter(parameter, fromRequestBody);
            }
        }

        return new NotAnnotatedParameterAdapter(parameter);
    }

    public boolean isMethodSatisfiedByRequiredQueryParams(Map<String, String[]> requestParamMap) {
        for (AbstractQueryParameterAdapter<RequiredQueryParam> param : this.requiredParameterAdapters) {
            if (!requestParamMap.containsKey(param.getParameterName())) {
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
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] mapControllerMethodParameters(HttpServletRequest request) throws ControllerMethodParameterMappingException {
        Object[] result = new Object[this.parameterAdapters.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = parameterAdapters[i].mapValue(request);
        }
        return result;
    }

    @Override
    public int compareTo(ControllerMethodAdapter other) {
        // reversed intentionally
        return Integer.compare(other.requiredParameterAdapters.size(), this.requiredParameterAdapters.size());
    }
}
