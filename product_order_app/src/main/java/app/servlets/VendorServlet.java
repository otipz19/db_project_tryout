package app.servlets;

import app.controllers.VendorController;
import controllerlib.controller.BaseController;
import controllerlib.servlet.BaseControllerServlet;
import jakarta.servlet.annotation.WebServlet;


@WebServlet("/vendor")
public class VendorServlet extends BaseControllerServlet {
    @Override
    protected Class<? extends BaseController> getControllerClass() {
        return VendorController.class;
    }
}
