package data.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Properties properties = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("database/database.properties")) {
            if (in == null) {
                throw new RuntimeException("Can't find file database.properties");
            }

            properties.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Error while loading database properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
}
