package cc.baka9.catseedlogin.bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.CommunicationAuth;

public class Communication {
    private static ServerSocket serverSocket;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void socketServerStopAsync() {
        CatSeedLogin.instance.runTaskAsync(Communication::socketServerStop);
    }

    public static synchronized void socketServerStop() {
        closeServerSocket(serverSocket);
    }

    public static void socketServerStartAsync() {
        CatSeedLogin.instance.runTaskAsync(Communication::socketServerStart);
    }

private static synchronized void socketServerStart() {
    try {
        InetAddress inetAddress = InetAddress.getByName(Config.BungeeCord.Host);
        serverSocket = new ServerSocket(Integer.parseInt(Config.BungeeCord.Port), 50, inetAddress);
        acceptConnections(serverSocket);
    } catch (UnknownHostException e) {
        CatSeedLogin.instance.getLogger().warning("无法解析域名或IP地址: " + e.getMessage());
    } catch (IOException e) {
        CatSeedLogin.instance.getLogger().warning("启动Socket服务器时发生错误: " + e.getMessage());
    }
}

private static void acceptConnections(ServerSocket serverSocket) {
    while (!serverSocket.isClosed()) {
        try {
            Socket socket = serverSocket.accept();
            executorService.submit(() -> handleRequest(socket));
        } catch (IOException e) {
            if (serverSocket.isClosed()) {
                break;
            }
            CatSeedLogin.instance.getLogger().warning("接受Socket连接时发生错误: " + e.getMessage());
        }
    }
}


private static void handleRequest(Socket socket) {
    BufferedReader bufferedReader = null;
    try {
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
    } catch (IOException e) {
        CatSeedLogin.instance.getLogger().warning("处理请求时发生错误: " + e.getMessage());
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
            } else {
                CatSeedLogin.instance.getLogger().warning("玩家 " + playerName + " 未找到在缓存中。");
            }
        });
    }
}

    private static void handleConnectRequest(Socket socket, String playerName) {
        CatScheduler.runTask(() -> {
            boolean result = LoginPlayerHelper.isLogin(playerName);
            sendConnectResultAsync(socket, result);
        });
    }

    private static void sendConnectResultAsync(Socket socket, boolean result) {
        executorService.submit(() -> {
            try (Socket autoCloseSocket = socket) {
                autoCloseSocket.getOutputStream().write(result ? 1 : 0);
                autoCloseSocket.getOutputStream().flush();
            } catch (IOException e) {
                CatSeedLogin.instance.getLogger().warning("发送连接结果时发生错误: " + e.getMessage());
            }
        });
    }

    private static void closeServerSocket(ServerSocket serverSocket) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                CatSeedLogin.instance.getLogger().warning("关闭Socket服务器时发生错误: " + e.getMessage());
            }
        }
    }
}
