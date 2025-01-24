package controllerlib.exceptions;

public abstract class MisconfiguredControllerException extends RuntimeException {
    public MisconfiguredControllerException(String msg) {
        super(msg);
    }
}
