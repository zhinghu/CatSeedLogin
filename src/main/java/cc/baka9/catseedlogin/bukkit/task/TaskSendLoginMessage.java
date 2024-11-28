package cc.baka9.catseedlogin.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;

public class TaskSendLoginMessage extends Task {
    @Override
    public void run() {
        if (!Cache.isLoaded) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                String playerName = player.getName();
                if (!LoginPlayerHelper.isLogin(playerName)) {
                    player.sendMessage(LoginPlayerHelper.isRegister(playerName) ? Config.Language.LOGIN_REQUEST : Config.Language.REGISTER_REQUEST);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
