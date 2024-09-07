package cc.baka9.catseedlogin.bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.CommunicationAuth;

/**
 * bukkit 与 bc 的通讯交流
 */
public class Communication {
    private static ServerSocket serverSocket;

    /**
     * 异步关闭 socket server
     */
    public static void socketServerStopAsync() {
        CatSeedLogin.instance.runTaskAsync(Communication::socketServerStop);
    }

    public static void socketServerStop() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                CatSeedLogin.instance.getLogger().warning("关闭Socket服务器时发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 异步启动 socket server 监听bc端发来的请求
     */
    public static void socketServerStartAsync() {
        CatSeedLogin.instance.runTaskAsync(Communication::socketServerStart);
    }

    /**
     * 启动 socket server 监听bc端发来的请求
     */
    private static void socketServerStart() {
        try {
            InetAddress inetAddress = InetAddress.getByName(Config.BungeeCord.Host);
            serverSocket = new ServerSocket(Integer.parseInt(Config.BungeeCord.Port), 50, inetAddress);
            while (!serverSocket.isClosed()) {
                try (Socket socket = serverSocket.accept()) {
                    handleRequest(socket);
                } catch (IOException e) {
                    CatSeedLogin.instance.getLogger().warning("接受Socket连接时发生错误: " + e.getMessage());
                }
            }
        } catch (UnknownHostException e) {
            CatSeedLogin.instance.getLogger().warning("无法解析域名或IP地址: " + e.getMessage());
        } catch (IOException e) {
            CatSeedLogin.instance.getLogger().warning("启动Socket服务器时发生错误: " + e.getMessage());
        }
    }

    /**
     * 处理请求
     */
    private static void handleRequest(Socket socket) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String requestType = bufferedReader.readLine();
            if (requestType == null) {
                return;
            }
            String playerName = bufferedReader.readLine();
            switch (requestType) {
                case "Connect":
                    handleConnectRequest(socket, playerName);
                    break;
                case "KeepLoggedIn":
                    String time = bufferedReader.readLine();
                    String sign = bufferedReader.readLine();
                    handleKeepLoggedInRequest(playerName, time, sign);
                    break;
                default:
                    CatSeedLogin.instance.getLogger().warning("未知请求类型: " + requestType);
                    break;
            }
        }
    }

    private static void handleKeepLoggedInRequest(String playerName, String time, String sign) {
        if (CommunicationAuth.encryption(playerName, time, Config.BungeeCord.AuthKey).equals(sign)) {
            CatScheduler.runTask(() -> {
                LoginPlayer lp = Cache.getIgnoreCase(playerName);
                if (lp != null) {
                    LoginPlayerHelper.add(lp);
                    Player player = Bukkit.getPlayerExact(playerName);
                    if (player != null) {
                        player.updateInventory();
                    }
                }
            });
        }
    }

private static void handleConnectRequest(Socket socket, String playerName) {
    CatScheduler.runTask(() -> {
        boolean result = LoginPlayerHelper.isLogin(playerName);
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                socket.getOutputStream().write(result ? 1 : 0);
                socket.getOutputStream().flush(); // 确保数据发送
            } catch (IOException e) {
                CatSeedLogin.instance.getLogger().warning("发送连接结果时发生错误: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    CatSeedLogin.instance.getLogger().warning("关闭Socket时发生错误: " + e.getMessage());
                }
            }
        });
    });
}

}
