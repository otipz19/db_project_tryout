package controllerlib;

public record ControllerResult<T>(T resultObject, int statusCode) {
}
