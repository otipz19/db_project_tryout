package app.contextlisteners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;

@WebListener
public class FlywayMigrationsApplier implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        var flyway = Flyway.configure()
                .dataSource("jdbc:postgresql://localhost:5432/product_order_db", "product_order_admin", "admin")
                .schemas("public")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        System.out.println("Applied SQL migrations!");
    }
}
