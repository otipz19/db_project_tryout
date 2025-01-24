package app.contextlisteners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.flywaydb.core.Flyway;

public class FlywayMigrationsApplier implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        var flyway = Flyway.configure()
                .schemas("public")
                .dataSource("jdbc:postgresql://localhost/product_order_db", "product_order_admin", "admin")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        System.out.println("Applied SQL migrations!");
    }
}
