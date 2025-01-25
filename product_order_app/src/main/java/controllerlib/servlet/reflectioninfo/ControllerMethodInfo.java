package controllerlib.servlet.reflectioninfo;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class ControllerMethodInfo {
    Method method;
    ControllerMethodParameterInfo[] parameterInfos;
}