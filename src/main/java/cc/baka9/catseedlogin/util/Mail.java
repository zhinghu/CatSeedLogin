package cc.baka9.catseedlogin.util;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;

import cc.baka9.catseedlogin.bukkit.Config;

public class Mail {

    private Mail() {
    }

    public static void sendMail(String receiveMailAccount, String subject, String content) throws Exception {

        Email email = new HtmlEmail();
        email.setHostName(Config.EmailVerify.EmailSmtpHost);
        email.setSmtpPort(Integer.parseInt(Config.EmailVerify.EmailSmtpPort));
        email.setAuthenticator(new DefaultAuthenticator(Config.EmailVerify.EmailAccount, Config.EmailVerify.EmailPassword));
        if (Config.EmailVerify.SSLAuthVerify) {
            email.setSSLOnConnect(true);
        } else {
            email.setStartTLSEnabled(true);
        }
        email.setFrom(Config.EmailVerify.EmailAccount, Config.EmailVerify.FromPersonal);
        email.setSubject(subject);
        email.setMsg(content);
        email.addTo(receiveMailAccount);
        email.setCharset("UTF-8");
        email.send();

    }

}
