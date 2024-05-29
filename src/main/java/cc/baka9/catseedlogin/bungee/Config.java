package cc.baka9.catseedlogin.bungee;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

public class Config {

    public static boolean Enable;
    public static String Host;
    public static int Port;
    public static String LoginServerName;
    public static String AuthKey;
    private static Logger logger = PluginMain.instance.getLogger();

    public static void load() {
        File dataFolder = PluginMain.instance.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        String fileName = "bungeecord.yml";
        File configFile = new File(dataFolder, fileName);
        if (!configFile.exists()) {
            try (InputStream in = PluginMain.instance.getResourceAsStream("bungee-resources/bungeecord.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ConfigurationProvider configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        try {
            Configuration config = configurationProvider.load(configFile);
            Enable = config.getBoolean("Enable");
            Host = config.getString("Host");
            Port = config.getInt("Port");
            LoginServerName = config.getString("LoginServerName");
            AuthKey = config.getString("AuthKey");

            // 合并日志输出
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Host: ").append(Host).append("\n");
            logBuilder.append("Port: ").append(Port).append("\n");
            logBuilder.append("LoginServerName: ").append(LoginServerName);
            logger.info(logBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}