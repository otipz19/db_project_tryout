package controllerlib.controller.method;

import com.google.common.collect.TreeMultiset;
import controllerlib.controller.BaseController;
import controllerlib.controller.ControllerResult;
import controllerlib.exceptions.InvalidControllerMethodReturnTypeException;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Contains all adapters of methods that match same route but differ in parameters
 */
public class ControllerMethodAdaptersContainer {
    private final TreeMultiset<ControllerMethodAdapter> methodAdapters = TreeMultiset.create();

    public static ControllerMethodAdaptersContainer createFromController(Class<? extends BaseController> controllerClass, Class<? extends Annotation> httpAnnotation) {
        var adaptersContainer = new ControllerMethodAdaptersContainer();

        Arrays.stream(controllerClass.getMethods())
                .filter(method -> method.isAnnotationPresent(httpAnnotation))
                .forEach(method -> {
                    adaptersContainer.addMethod(controllerClass, method);
                });

        return adaptersContainer;
    }

    public void addMethod(Class<? extends BaseController> controllerClass, Method method) {
        if (!method.getReturnType().equals(ControllerResult.class)) {
            throw new InvalidControllerMethodReturnTypeException(method.getName());
        }
        this.methodAdapters.add(new ControllerMethodAdapter(controllerClass, method));
    }

    public ControllerMethodAdapter chooseMethodAdapter(HttpServletRequest req) {
        for (var methodAdapter : methodAdapters) {
            if (methodAdapter.isMethodSatisfiedByRequiredQueryParams(req.getParameterMap())) {
                return methodAdapter;
            }
        }
        return null;
    }
}
