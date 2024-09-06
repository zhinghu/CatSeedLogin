package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

public class SQLite extends SQL {
    private Connection connection;

    public SQLite(JavaPlugin javaPlugin) {
        super(javaPlugin);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
            return connection;
        } catch (SQLException e) {
            return null;
        }
    }

    private Connection createConnection() throws SQLException {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/accounts.db");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }
}
