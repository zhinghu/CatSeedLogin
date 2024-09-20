package cc.baka9.catseedlogin.bungee;

import java.net.Socket;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import cc.baka9.catseedlogin.util.CommunicationAuth;

/**
 * bc 与 bukkit 的通讯交流
 */
public class Communication {
    private static final String HOST = Config.Host;
    private static final int PORT = Config.Port;

    public static int sendConnectRequest(String playerName) {
        return sendRequest("Connect", playerName);
    }

    public static void sendKeepLoggedInRequest(String playerName) {
        long currentTime = System.currentTimeMillis();
        String time = String.valueOf(currentTime);
        String sign = CommunicationAuth.encryption(playerName, time, Config.AuthKey);
        sendRequest("KeepLoggedIn", playerName, time, sign);
    }

    private static int sendRequest(String requestType, String... messages) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            writeMessage(writer, requestType, messages);
            return socket.getInputStream().read();
        } catch (IOException e) {
            return 0;
        }
    }

    private static void writeMessage(BufferedWriter writer, String requestType, String... messages) throws IOException {
        writer.write(requestType);
        writer.newLine();
        for (String message : messages) {
            writer.write(message);
            writer.newLine();
        }
        writer.flush();
    }
}
