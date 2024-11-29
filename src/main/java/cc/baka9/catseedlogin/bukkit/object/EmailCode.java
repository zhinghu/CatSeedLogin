package cc.baka9.catseedlogin.bukkit.object;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import cc.baka9.catseedlogin.util.Util;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCode {
    public enum Type { Bind, ResetPassword }
    private static Map<String, EmailCode> bindMap = new ConcurrentHashMap<>(10);
    private static Map<String, EmailCode> resetPasswordMap = new ConcurrentHashMap<>(10);
    private String name, email, code;
    private long durability, createTime;

    private EmailCode(String name, String email, long durability) {
        this.name = name; this.email = email; this.durability = durability;
        this.createTime = System.currentTimeMillis(); this.code = Util.randomStr();
    }

    public static EmailCode create(String name, String email, long durability, Type type) {
        EmailCode emailCode = new EmailCode(name, email, durability);
        if (type == Type.Bind) bindMap.put(name, emailCode);
        else if (type == Type.ResetPassword) resetPasswordMap.put(name, emailCode);
        return emailCode;
    }

    public static Optional<EmailCode> getByName(String name, Type type) {
        clear();
        return (type == Type.Bind && bindMap.containsKey(name)) ? Optional.of(bindMap.get(name)) :
               (type == Type.ResetPassword && resetPasswordMap.containsKey(name)) ? Optional.of(resetPasswordMap.get(name)) :
               Optional.empty();
    }

    public static void removeByName(String name, Type type) {
        clear();
        if (type == Type.Bind) bindMap.remove(name);
        else if (type == Type.ResetPassword) resetPasswordMap.remove(name);
    }

    private static void clear() {
        long now = System.currentTimeMillis();
        bindMap.entrySet().removeIf(next -> now - next.getValue().getCreateTime() > next.getValue().getDurability());
        resetPasswordMap.entrySet().removeIf(next -> now - next.getValue().getCreateTime() > next.getValue().getDurability());
    }
}
