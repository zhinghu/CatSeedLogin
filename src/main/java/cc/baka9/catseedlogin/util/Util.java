package cc.baka9.catseedlogin.util;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;

public class Util {
    private static final Pattern passwordDifficultyRegex = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Random random = new SecureRandom();

    public static boolean passwordIsDifficulty(String pwd) {
        return !passwordDifficultyRegex.matcher(pwd).find();
    }

    public static String time2Str(long time) {
        synchronized (sdf) {
            return sdf.format(new Date(time));
        }
    }

    public static boolean checkMail(String e_mail) {
        return e_mail.matches("[a-zA-Z0-9_]+@[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+");
    }

    public static String randomStr() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    public static boolean isOSLinux() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("linux");
    }
}
