package cc.baka9.catseedlogin.bungee;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import cc.baka9.catseedlogin.util.CommunicationAuth;

/**
 * bc 与 bukkit 的通讯交流
 */
public class Communication {
    private static final String HOST = Config.Host;
    private static final int PORT = Config.Port;

    public static int sendConnectRequest(String playerName) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            writeMessage(writer, "Connect", playerName);
            return socket.getInputStream().read();
        } catch (IOException e) {
            return 0;
        }
    }

    public static void sendKeepLoggedInRequest(String playerName) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            long currentTime = System.currentTimeMillis();
            String time = String.valueOf(currentTime);
            String sign = CommunicationAuth.encryption(playerName, time, Config.AuthKey);
            writeMessage(writer, "KeepLoggedIn", playerName, time, sign);
        } catch (IOException e) {
        }
    }

    private static void writeMessage(BufferedWriter writer, String... messages) throws IOException {
        for (String message : messages) {
            writer.write(message);
            writer.newLine();
        }
        writer.flush();
    }
}
