package app.servlets;

import app.controllers.VendorController;
import controllerlib.exposed.BaseController;
import controllerlib.internal.servlet.BaseControllerServlet;
import jakarta.servlet.annotation.WebServlet;


@WebServlet("/vendor")
public class VendorServlet extends BaseControllerServlet {
    @Override
    protected Class<? extends BaseController> getControllerClass() {
        return VendorController.class;
    }
}
