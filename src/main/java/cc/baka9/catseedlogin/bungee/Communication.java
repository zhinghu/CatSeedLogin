package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.util.CommunicationAuth;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * bc 与 bukkit 的通讯交流
 */
public class Communication {
    private static final String HOST = Config.Host;
    private static final int PORT = Config.Port;

    public static int sendConnectRequest(String playerName) {
        try (Socket socket = getSocket(); BufferedWriter bufferedWriter = getSocketBufferedWriter(socket)) {
            bufferedWriter.write("Connect");
            bufferedWriter.newLine();
            bufferedWriter.write(playerName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            return socket.getInputStream().read();
        } catch (IOException e) {
            handleIOException(e);
        }
        return 0;
    }

    public static void sendKeepLoggedInRequest(String playerName) {
        try (Socket socket = getSocket(); BufferedWriter bufferedWriter = getSocketBufferedWriter(socket)) {
            bufferedWriter.write("KeepLoggedIn");
            bufferedWriter.newLine();
            bufferedWriter.write(playerName);
            bufferedWriter.newLine();
            long currentTime = System.currentTimeMillis();
            String time = String.valueOf(currentTime);
            bufferedWriter.write(time);
            bufferedWriter.newLine();
            String sign = CommunicationAuth.encryption(playerName, time, Config.AuthKey);
            bufferedWriter.write(sign);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private static Socket getSocket() throws IOException {
        try {
            return new Socket(HOST, PORT);
        } catch (IOException e) {
            ProxyServer.getInstance().getLogger().warning("§c请检查装载登录插件的子服是否在 bungeecord.yml 中开启了bungeecord功能，以及Host和Port是否与bc端的配置相同");
            throw new IOException(e);
        }
    }

    private static BufferedWriter getSocketBufferedWriter(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private static void handleIOException(IOException e) {
        ProxyServer.getInstance().getLogger().severe("发生 I/O 异常: " + e.getMessage());
    }
}