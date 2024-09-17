package cc.baka9.catseedlogin.bungee;

import java.net.Socket;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import net.md_5.bungee.api.ProxyServer;
import cc.baka9.catseedlogin.util.CommunicationAuth;

/**
 * bc 与 bukkit 的通讯交流
 */
public class Communication {
    private static final String HOST = Config.Host;
    private static final int PORT = Config.Port;

    public static int sendConnectRequest(String playerName) {
        Socket socket = null;
        BufferedWriter writer = null;
        try {
            socket = getSocket();
            writer = getSocketBufferedWriter(socket);
            writeMessage(writer, "Connect", playerName);
            return socket.getInputStream().read();
        } catch (IOException e) {
            handleIOException(e);
            return 0;
        }
    }

    public static void sendKeepLoggedInRequest(String playerName) {
        Socket socket = null;
        BufferedWriter writer = null;
        try {
            socket = getSocket();
            writer = getSocketBufferedWriter(socket);
            long currentTime = System.currentTimeMillis();
            String time = String.valueOf(currentTime);
            String sign = CommunicationAuth.encryption(playerName, time, Config.AuthKey);
            writeMessage(writer, "KeepLoggedIn", playerName, time, sign);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private static Socket getSocket() throws IOException {
        try {
            return new Socket(HOST, PORT);
        } catch (IOException e) {
            ProxyServer.getInstance().getLogger().warning("§c请检查装载登录插件的子服是否在 bungeecord.yml 中开启了bungeecord功能，以及Host和Port是否与bc端的配置相同");
            throw e;
        }
    }

    private static BufferedWriter getSocketBufferedWriter(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private static void writeMessage(BufferedWriter writer, String... messages) throws IOException {
        for (String message : messages) {
            writer.write(message);
            writer.newLine();
        }
        writer.flush();
    }

    private static void handleIOException(IOException e) {
        ProxyServer.getInstance().getLogger().severe("发生 I/O 异常: " + e.getMessage());
    }
}
