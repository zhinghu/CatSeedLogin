package cc.baka9.catseedlogin.bungee;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Bungee Cord 监听事件类
 */
public class Listeners implements Listener {

    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private final List<String> loggedInPlayerList = new CopyOnWriteArrayList<>();

    /**
     * 登录之前不能输入bc指令
     */
    @EventHandler
    public void onChat(ChatEvent event) {
        if (!event.isProxyCommand() || !(event.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String playerName = player.getName();

        if (!loggedInPlayerList.contains(playerName)) {
            event.setCancelled(true);
            handleLogin(player, event.getMessage());
        }
    }

    /**
     * 玩家切换子服时，检查bc端该玩家的登录状态，
     * 如果没有登录，在登录服获取登录状态后更新bc端该玩家的登录状态，
     * 如果登录服依然未登录，强制切换目标服务器为登录服
     */
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ServerInfo target = event.getTarget();
        if (event.isCancelled() || target.getName().equals(Config.LoginServerName)) return;
        ProxiedPlayer player = event.getPlayer();
        String playerName = player.getName();

        if (!loggedInPlayerList.contains(playerName)) {
            handleLogin(player, null);
            event.setTarget(proxyServer.getServerInfo(Config.LoginServerName));
        }
    }

    /**
     * 玩家切换到登录服务之后，如果bc端已登录模式，使用登录状态更新子服务器登录状态，避免玩家需要重新登录
     */
    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getServer().getInfo().getName().equals(Config.LoginServerName)) {
            ProxiedPlayer player = event.getPlayer();
            String playerName = player.getName();

            if (loggedInPlayerList.contains(playerName)) {
                PluginMain.runAsync(() -> Communication.sendKeepLoggedInRequest(playerName));
            }
        }
    }

    /**
     * 玩家离线时，从bc端删除玩家的登录状态
     */
    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        loggedInPlayerList.remove(event.getPlayer().getName());
    }

    /**
     * 玩家在登录前，检查bc端和子服务器的登录状态，如果任一已登录，阻止连接
     */
    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        String playerName = event.getConnection().getName();
        try {
            if (loggedInPlayerList.contains(playerName) || Communication.sendConnectRequest(playerName) == 1) {
                event.setCancelReason(new TextComponent("您已经登录，请勿重复登录。"));
                event.setCancelled(true);
            }
        } catch (Exception e) {
            event.setCancelReason(new TextComponent("发生错误，请稍后再试。"));
            event.setCancelled(true);
            throw e;
        }
    }

    /**
     * 处理玩家登录逻辑
     */
    private void handleLogin(ProxiedPlayer player, String message) {
        String playerName = player.getName();
        PluginMain.runAsync(() -> {
            if (Communication.sendConnectRequest(playerName) == 1) {
                loggedInPlayerList.add(playerName);
                if (message != null) {
                    proxyServer.getPluginManager().dispatchCommand(player, message.substring(1));
                }
            }
        });
    }
}
