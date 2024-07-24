package cc.baka9.catseedlogin.bukkit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CatScheduler {
    // folia check
    public static boolean folia = isFolia();
    private static Method teleportAsync;

    static {
        // init reflect for folia
        if (folia) {
            try {
                teleportAsync = Player.class.getMethod("teleportAsync", Location.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isFolia() {
        return CatSeedLogin.morePaperLib.scheduling().isUsingFolia();
    }

    public static void teleport(Player player, Location location) {
        if (!folia) {
            player.teleport(location);
            return;
        }

        CatSeedLogin.morePaperLib.scheduling().entitySpecificScheduler(player).run(() -> {
            try {
                teleportAsync.invoke(player, location);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, null);

    }

    public static void updateInventory(Player player) {
        CatSeedLogin.morePaperLib.scheduling().entitySpecificScheduler(player).run(player::updateInventory, null);
    }

    public static ScheduledTask runTaskAsync(Runnable runnable) {
        return CatSeedLogin.morePaperLib.scheduling().asyncScheduler().run(runnable);
    }

    public static ScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        return CatSeedLogin.morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(runnable,delay == 0 ? 1 : delay,period);
    }

    public static ScheduledTask runTask(Runnable runnable) {
        return CatSeedLogin.morePaperLib.scheduling().globalRegionalScheduler().run(runnable);
    }

    public static ScheduledTask runTaskLater(Runnable runnable, long delay) {
        return CatSeedLogin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(runnable,delay);
    }


}
