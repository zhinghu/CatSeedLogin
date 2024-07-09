package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

public class SQLite extends SQL {
    private Connection connection;

    public SQLite(JavaPlugin javaPlugin){
        super(javaPlugin);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/accounts.db");
            }
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
