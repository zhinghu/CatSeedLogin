package cc.baka9.catseedlogin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationAuth {

    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // 适当地处理异常
            e.printStackTrace();
        }
    }

    public static String encryption(String... args) {
        String paramString = String.join("", args);
        byte[] arrayOfByte = messageDigest.digest(paramString.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (byte value : arrayOfByte) {
            int unsignedByte = value & 0xff;
            if (unsignedByte < 16) stringBuilder.append("0");
            stringBuilder.append(Integer.toHexString(unsignedByte));
        }
        return stringBuilder.toString().toLowerCase();
    }

}