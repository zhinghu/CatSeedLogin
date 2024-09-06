package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class SQLite extends SQL {
    private Connection connection;
    private final Logger logger;

    public SQLite(JavaPlugin javaPlugin) {
        super(javaPlugin);
        this.logger = javaPlugin.getLogger();
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get database connection", e);
            return null;
        }
    }

    private Connection createConnection() throws SQLException {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/accounts.db");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "SQLite JDBC driver not found", e);
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }
}
