package controllerlib.servlet;

import lombok.Data;

import java.lang.reflect.Method;

@Data
class ControllerMethodInfo {
    Method method;
    ControllerMethodParameterInfo[] parameterInfos;
}