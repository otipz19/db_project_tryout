package app.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

@WebServlet("/")
public class HelloWorldServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = "jdbc:postgresql://localhost/product_order_db";
        Properties props = new Properties();
        props.setProperty("user", "product_order_admin");
        props.setProperty("password", "admin");
        try (Connection conn = DriverManager.getConnection(url, props)) {
            var statement = conn.createStatement();
            String sql = """
                    SELECT * FROM vendor;
                    """;
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                var writer = resp.getWriter();
                writer.println(resultSet.getString("name"));
            }
            resp.setContentType("text/plain");
            resp.setStatus(200);
        } catch (SQLException e) {
            System.out.println("SQL has failed!");
            resp.setStatus(500);
        }
    }
}
