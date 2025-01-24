package app.repositories;

import app.entity.VendorEntity;
import app.exceptions.EntityNotFoundException;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class VendorRepository {
    private static final String DB_URL = "jdbc:postgresql://localhost/product_order_db";
    private static final String DB_USER = "product_order_admin";
    private static final String DB_PASSWORD = "admin";

    public List<VendorEntity> get() throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String sql = """
                    SELECT * FROM vendor;
                    """;
            ResultSet resultSet = statement.executeQuery(sql);
            return readResultSet(resultSet);
        }
    }

    public VendorEntity get(int id) throws SQLException, EntityNotFoundException {
        try (Connection connection = getConnection()) {
            String sql = """
                    SELECT * FROM vendor
                    WHERE id = ?;
                    """;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            List<VendorEntity> result = readResultSet(resultSet);
            if (result.isEmpty()) {
                throw new EntityNotFoundException(id);
            }
            return result.get(0);
        }
    }

    private static List<VendorEntity> readResultSet(ResultSet resultSet) throws SQLException {
        List<VendorEntity> result = new LinkedList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String address = resultSet.getString("address");
            result.add(new VendorEntity(id, name, address));
        }
        return result;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
