package controllerlib.servlet.reflectioninfo;

import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

@Data
public class ControllerMethodParameterInfo {
    Parameter parameter;
    String name = "";
    ControllerMethodParameterType controllerParameterType = ControllerMethodParameterType.NOT_ANNOTATED;
    Annotation annotation;
}
