package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class SQL {
    protected JavaPlugin plugin;

    public SQL(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() throws Exception {
        flush(new BufferStatement("CREATE TABLE IF NOT EXISTS accounts (name CHAR(255), password CHAR(255), email CHAR(255), ips CHAR(255), lastAction TIMESTAMP)"));

        try {
            flush(new BufferStatement("ALTER TABLE accounts ADD email CHAR(255)"));
        } catch (Exception e) {
            if (!e.getMessage().toLowerCase().contains("duplicate column name")) throw e;
        }

        try {
            flush(new BufferStatement("ALTER TABLE accounts ADD ips CHAR(255)"));
        } catch (Exception e) {
            if (!e.getMessage().toLowerCase().contains("duplicate column name")) throw e;
        }
    }

    public void add(LoginPlayer lp) throws Exception {
        flush(new BufferStatement("INSERT INTO accounts (name, password, lastAction, email, ips) VALUES (?, ?, ?, ?, ?)",
            lp.getName(), lp.getPassword(), new Date(), lp.getEmail(), lp.getIps()));
        Cache.refresh(lp.getName());
    }

    public void del(String name) throws Exception {
        flush(new BufferStatement("DELETE FROM accounts WHERE name = ?", name));
        Cache.refresh(name);
    }

    public void edit(LoginPlayer lp) throws Exception {
        flush(new BufferStatement("UPDATE accounts SET password = ?, lastAction = ?, email = ?, ips = ? WHERE name = ?",
            lp.getPassword(), new Date(), lp.getEmail(), lp.getIps(), lp.getName()));
        Cache.refresh(lp.getName());
    }

    public LoginPlayer get(String name) throws Exception {
        PreparedStatement ps = new BufferStatement("SELECT * FROM accounts WHERE name = ?", name).prepareStatement(getConnection());
        ResultSet resultSet = ps.executeQuery();
        LoginPlayer lp = null;
        if (resultSet.next()) {
            lp = new LoginPlayer(name, resultSet.getString("password"));
            lp.setLastAction(resultSet.getTimestamp("lastAction").getTime());
            lp.setEmail(resultSet.getString("email"));
            lp.setIps(resultSet.getString("ips"));
        }
        resultSet.close();
        ps.close();
        return lp;
    }

    public List<LoginPlayer> getAll() throws Exception {
        PreparedStatement ps = new BufferStatement("SELECT * FROM accounts").prepareStatement(getConnection());
        ResultSet resultSet = ps.executeQuery();
        List<LoginPlayer> lps = new ArrayList<>();
        while (resultSet.next()) {
            LoginPlayer lp = new LoginPlayer(resultSet.getString("name"), resultSet.getString("password"));
            lp.setLastAction(resultSet.getTimestamp("lastAction").getTime());
            lp.setEmail(resultSet.getString("email"));
            lp.setIps(resultSet.getString("ips"));
            lps.add(lp);
        }
        return lps;
    }

    public List<LoginPlayer> getLikeByIp(String ip) throws Exception {
        PreparedStatement ps = new BufferStatement("SELECT * FROM accounts WHERE ips LIKE ?", "%" + ip + "%").prepareStatement(getConnection());
        ResultSet resultSet = ps.executeQuery();
        List<LoginPlayer> lps = new ArrayList<>();
        while (resultSet.next()) {
            LoginPlayer lp = new LoginPlayer(resultSet.getString("name"), resultSet.getString("password"));
            lp.setLastAction(resultSet.getTimestamp("lastAction").getTime());
            lp.setEmail(resultSet.getString("email"));
            lp.setIps(resultSet.getString("ips"));
            lps.add(lp);
        }
        return lps;
    }

    public abstract Connection getConnection() throws Exception;

    public void flush(BufferStatement bufferStatement) throws Exception {
        PreparedStatement ps = bufferStatement.prepareStatement(getConnection());
        ps.executeUpdate();
        ps.close();
    }
}
