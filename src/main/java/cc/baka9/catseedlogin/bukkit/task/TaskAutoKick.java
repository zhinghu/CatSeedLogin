package cc.baka9.catseedlogin.bukkit.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;

public class TaskAutoKick extends Task {
    public Map<String, Long> playerJoinTime = new ConcurrentHashMap<>();

    @Override
    public void run() {
        if (!Cache.isLoaded || Config.Settings.AutoKick < 1) return;

        long autoKickMs = Config.Settings.AutoKick * 1000L;
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getName();
            try {
                if (!LoginPlayerHelper.isLogin(playerName)) {
                    playerJoinTime.putIfAbsent(playerName, now);
                    if (now - playerJoinTime.get(playerName) > autoKickMs) {
                        player.kickPlayer(Config.Language.AUTO_KICK.replace("{time}", String.valueOf(Config.Settings.AutoKick)));
                    }
                } else {
                    playerJoinTime.remove(playerName);
                }
            } catch (Exception e) {
                // 记录错误日志
                e.printStackTrace();
            }
        }
    }
}
