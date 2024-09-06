package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import cc.baka9.catseedlogin.bukkit.Config;

public class MySQL extends SQL {
    private Connection connection;

    public MySQL(JavaPlugin javaPlugin) {
        super(javaPlugin);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (isConnectionValid()) {
            return this.connection;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://" + Config.MySQL.Host + ":" + Config.MySQL.Port + "/" + Config.MySQL.Database + "?characterEncoding=UTF-8",
                    Config.MySQL.User, Config.MySQL.Password
            );
            return this.connection;
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("无法建立数据库连接", e);
        }
    }

    private boolean isConnectionValid() throws SQLException {
        return this.connection != null && !this.connection.isClosed() && this.connection.isValid(10);
    }
}
