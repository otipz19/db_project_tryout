package controllerlib.servlet;

import lombok.Data;

import java.lang.reflect.Parameter;

@Data
class ControllerMethodParameterInfo {
    Parameter parameter;
    String name = "";
    ControllerMethodParameterType controllerParameterType = ControllerMethodParameterType.NOT_ANNOTATED;
}
