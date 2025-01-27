package controllerlib.exposed;

public record ControllerResult<T>(T resultObject, int statusCode) {
}
