package controllerlib.servlet;

enum ControllerMethodParameterType {
    NOT_ANNOTATED,
    REQUIRED_QUERY_PARAM,
    NOT_REQUIRED_QUERY_PARAM,
    FROM_REQUEST_BODY,
}
