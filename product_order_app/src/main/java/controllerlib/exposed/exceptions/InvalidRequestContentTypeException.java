package controllerlib.exposed.exceptions;

public class InvalidRequestContentTypeException extends ControllerMethodParameterMappingException {
    public InvalidRequestContentTypeException(String expected, String actual) {
        super("Invalid request content type. Expected: " + expected + ". Actual: " + actual);
    }
}
