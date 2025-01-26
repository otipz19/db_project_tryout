package controllerlib.controller;

public record ControllerResult<T>(T resultObject, int statusCode) {
}
