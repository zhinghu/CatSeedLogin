package cc.baka9.catseedlogin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationAuth {

    private static final MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static String encryption(String... args) {
        String paramString = String.join("", args);
        byte[] arrayOfByte = messageDigest.digest(paramString.getBytes());
        StringBuilder stringBuilder = new StringBuilder(arrayOfByte.length * 2);
        for (byte value : arrayOfByte) {
            stringBuilder.append(String.format("%02x", value & 0xff));
        }
        return stringBuilder.toString();
    }
}
