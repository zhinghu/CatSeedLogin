package cc.baka9.catseedlogin.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import space.arim.morepaperlib.scheduling.ScheduledTask;

public class CatScheduler {
    private static final boolean folia = CatSeedLogin.morePaperLib.scheduling().isUsingFolia();
    private static final Method teleportAsync = initTeleportAsync();

    private static Method initTeleportAsync() {
        if (folia) {
            try {
                return Player.class.getMethod("teleportAsync", Location.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static void teleport(Player player, Location location) {
        if (!folia) {
            player.teleport(location);
            return;
        }
        CatSeedLogin.morePaperLib.scheduling().entitySpecificScheduler(player).run(() -> {
            try {
                if (teleportAsync != null) {
                    teleportAsync.invoke(player, location);
                }
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
        return CatSeedLogin.morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(runnable, delay == 0 ? 1 : delay, period);
    }

    public static ScheduledTask runTask(Runnable runnable) {
        return CatSeedLogin.morePaperLib.scheduling().globalRegionalScheduler().run(runnable);
    }

    public static ScheduledTask runTaskLater(Runnable runnable, long delay) {
        return CatSeedLogin.morePaperLib.scheduling().globalRegionalScheduler().runDelayed(runnable, delay);
    }
}
