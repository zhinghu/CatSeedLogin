package cc.baka9.catseedlogin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationAuth {

    public static String encryption(String... args) {
        String paramString = String.join("", args);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(paramString.getBytes());
            byte[] arrayOfByte = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte value : arrayOfByte) {
                int unsignedByte = value & 0xff;
                if (unsignedByte < 16) stringBuilder.append("0");
                stringBuilder.append(Integer.toHexString(unsignedByte));
            }
            return stringBuilder.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

}
