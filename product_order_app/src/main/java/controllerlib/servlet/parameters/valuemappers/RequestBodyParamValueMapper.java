package controllerlib.servlet.parameters.valuemappers;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllerlib.annotations.FromRequestBody;
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
            var jsonMapper = new ObjectMapper();
            FromRequestBody annotation = (FromRequestBody) parameterInfo.getAnnotation();
            if (annotation.isGenericCollection()) {
                JavaType type = jsonMapper.getTypeFactory().constructCollectionType(annotation.collectionType(), annotation.elementType());
                return jsonMapper.readValue(request.getReader(), type);
            } else {
                return jsonMapper.readValue(request.getReader(), parameterInfo.getParameter().getType());
            }
        } catch (IOException e) {
            throw new ControllerMethodParameterMappingException("Error while reading request body");
        }
    }
}
