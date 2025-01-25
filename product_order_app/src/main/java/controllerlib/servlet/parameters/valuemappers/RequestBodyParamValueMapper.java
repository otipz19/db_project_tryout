package controllerlib.servlet.parameters.valuemappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllerlib.exceptions.ControllerMethodParameterMappingException;
import controllerlib.exceptions.InvalidRequestContentTypeException;
import controllerlib.servlet.reflectioninfo.ControllerMethodParameterInfo;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class RequestBodyParamValueMapper implements ControllerMethodParameterValueMapper {
    @Override
    public Object mapValue(ControllerMethodParameterInfo parameterInfo, HttpServletRequest request) throws ControllerMethodParameterMappingException {
        String expectedContentType = "application/json";
        String actualContentType = request.getContentType();
        if (actualContentType == null || !actualContentType.equals(expectedContentType)) {
            throw new InvalidRequestContentTypeException(expectedContentType, actualContentType);
        }
        try {
            return new ObjectMapper().readValue(request.getReader(), parameterInfo.getParameter().getType());
        } catch (IOException e) {
            throw new ControllerMethodParameterMappingException("Error while reading request body");
        }
    }
}
