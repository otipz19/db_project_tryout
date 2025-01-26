package controllerlib.controller.method.reflectioninfo;

import controllerlib.annotations.RequiredQueryParam;
import controllerlib.controller.BaseController;
import controllerlib.controller.ControllerResult;
import controllerlib.controller.method.ControllerMethodAdapter;
import controllerlib.exceptions.InvalidControllerMethodReturnTypeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ControllerMethodAdapterCreator {
    public static ControllerMethodAdapter[] buildControllerMethodAdapters(Class<? extends BaseController> controllerClass, Class<? extends Annotation> httpAnnotation) {
        List<Method> methods = Arrays.stream(controllerClass.getMethods())
                .filter(method -> method.isAnnotationPresent(httpAnnotation))
                .sorted((o1, o2) -> {
                    int first = countRequiredQueryParamsOfMethod(o1);
                    int second = countRequiredQueryParamsOfMethod(o2);
                    return Integer.compare(second, first);
                }).toList();

        var controllerMethodInfos = new ControllerMethodAdapter[methods.size()];

        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (!method.getReturnType().equals(ControllerResult.class)) {
                throw new InvalidControllerMethodReturnTypeException(method.getName());
            }
            controllerMethodInfos[i] = new ControllerMethodAdapter(controllerClass, method);
        }

        return controllerMethodInfos;
    }

    private static int countRequiredQueryParamsOfMethod(Method method) {
        return (int) Arrays.stream(method.getParameters())
                .filter(param -> param.isAnnotationPresent(RequiredQueryParam.class))
                .count();
    }
}
