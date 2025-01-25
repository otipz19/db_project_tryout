package controllerlib;

import jakarta.servlet.http.HttpServletResponse;

public class BaseController {
    protected <T> ControllerResult<T> Ok(T resultObject) {
        return new ControllerResult<>(resultObject, HttpServletResponse.SC_OK);
    }

    protected <T> ControllerResult<T> InternalServerError() {
        return new ControllerResult<>(null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    protected <T> ControllerResult<T> NotFound() {
        return new ControllerResult<>(null, HttpServletResponse.SC_NOT_FOUND);
    }

    protected <T> ControllerResult<T> BadRequest() {
        return new ControllerResult<>(null, HttpServletResponse.SC_BAD_REQUEST);
    }
}
