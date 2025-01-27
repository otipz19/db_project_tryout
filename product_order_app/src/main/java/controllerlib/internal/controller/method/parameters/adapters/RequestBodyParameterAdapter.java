package controllerlib.internal.controller.method.parameters.adapters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllerlib.exposed.annotations.FromRequestBody;
import controllerlib.internal.controller.method.parameters.adapters.base.ControllerMethodParameterAdapter;
import controllerlib.exposed.exceptions.ControllerMethodParameterMappingException;
import controllerlib.exposed.exceptions.InvalidRequestContentTypeException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.lang.reflect.Parameter;

public class RequestBodyParameterAdapter extends ControllerMethodParameterAdapter<FromRequestBody> {
    private static final String EXPECTED_CONTENT_TYPE = "application/json";

    private final ObjectMapper jsonMapper;

    public RequestBodyParameterAdapter(Parameter parameter, FromRequestBody annotation) {
        super(parameter, annotation);
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public Object mapValue(HttpServletRequest request) throws ControllerMethodParameterMappingException {
        String actualContentType = request.getContentType();
        if (actualContentType == null || !actualContentType.equals(EXPECTED_CONTENT_TYPE)) {
            throw new InvalidRequestContentTypeException(EXPECTED_CONTENT_TYPE, actualContentType);
        }

        try {
            if (annotation.isGenericCollection()) {
                JavaType type = jsonMapper
                        .getTypeFactory()
                        .constructCollectionType(annotation.collectionType(), annotation.elementType());
                return jsonMapper.readValue(request.getReader(), type);
            } else {
                return jsonMapper.readValue(request.getReader(), parameter.getType());
            }
        } catch (IOException e) {
            throw new ControllerMethodParameterMappingException("Error while reading request body");
        }
    }
}
