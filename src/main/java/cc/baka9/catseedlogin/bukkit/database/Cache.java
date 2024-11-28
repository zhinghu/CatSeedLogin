package cc.baka9.catseedlogin.bukkit.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;

public class Cache {
    private static final Map<String, LoginPlayer> PLAYER_HASHTABLE = new ConcurrentHashMap<>();
    public static volatile boolean isLoaded = false;

    public static List<LoginPlayer> getAllLoginPlayer() {
        synchronized (PLAYER_HASHTABLE) {
            return new ArrayList<>(PLAYER_HASHTABLE.values());
        }
    }

    public static LoginPlayer getIgnoreCase(String name) {
        return PLAYER_HASHTABLE.get(name.toLowerCase());
    }

    public static void refreshAll() {
        isLoaded = false;
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                List<LoginPlayer> newCache = CatSeedLogin.sql.getAll();
                synchronized (PLAYER_HASHTABLE) {
                    PLAYER_HASHTABLE.clear();
                    newCache.forEach(p -> PLAYER_HASHTABLE.put(p.getName().toLowerCase(), p));
                }
                CatSeedLogin.instance.getLogger().info("缓存加载 " + PLAYER_HASHTABLE.size() + " 个数据");
                isLoaded = true;
            } catch (Exception e) {
                CatSeedLogin.instance.getLogger().warning("数据库错误,无法更新缓存!");
                e.printStackTrace();
            }
        });
    }

    public static void refresh(String name) {
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                LoginPlayer newLp = CatSeedLogin.sql.get(name);
                String key = name.toLowerCase();
                synchronized (PLAYER_HASHTABLE) {
                    if (newLp != null) {
                        PLAYER_HASHTABLE.put(key, newLp);
                    } else {
                        PLAYER_HASHTABLE.remove(key);
                    }
                }
                CatSeedLogin.instance.getLogger().info("缓存加载 " + PLAYER_HASHTABLE.size() + " 个数据");
            } catch (Exception e) {
                CatSeedLogin.instance.getLogger().warning("数据库错误,无法更新缓存!");
                e.printStackTrace();
            }
        });
    }
}
