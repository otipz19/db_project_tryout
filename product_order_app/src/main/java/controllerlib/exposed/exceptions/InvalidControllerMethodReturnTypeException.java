package controllerlib.exposed.exceptions;

public class InvalidControllerMethodReturnTypeException extends MisconfiguredControllerException {
    public InvalidControllerMethodReturnTypeException(String methodName) {
        super("Method " + methodName + " has wrong return type. It must be ControllerResult");
    }
}
