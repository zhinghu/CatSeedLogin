package cc.baka9.catseedlogin.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class CatScheduler {

    // folia check
    public static boolean folia = isFolia();
    public static boolean isFolia(){
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    private static Object asyncScheduler;
    private static Object globalRegionScheduler;
    private static Method runNow;
    private static Method runAtFixedRate;
    private static Method runDelayed;
    private static Method run;
    private static Method cancel;
    private static Method teleportAsync;
    private static Class<?> scheduledTask;


    static {
        // init reflect for folia
        if (folia){
            try {

                // folia scheduler
                String asyncSchedulerName = "io.papermc.paper.threadedregions.scheduler.AsyncScheduler";
                String globalRegionSchedulerName = "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler";
                String scheduledTaskName = "io.papermc.paper.threadedregions.scheduler.ScheduledTask";

                Method getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
                Method getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");

                scheduledTask = Class.forName(scheduledTaskName);

                asyncScheduler = getAsyncScheduler.invoke(Bukkit.class);
                globalRegionScheduler = getGlobalRegionScheduler.invoke(Bukkit.class);

                runNow = Class.forName(asyncSchedulerName).getMethod("runNow", Plugin.class, Consumer.class);
                runAtFixedRate = Class.forName(globalRegionSchedulerName).getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
                runDelayed = Class.forName(globalRegionSchedulerName).getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                run = Class.forName(globalRegionSchedulerName).getMethod("run", Plugin.class, Consumer.class);
                cancel = scheduledTask.getMethod("cancel");

                // folia async teleport
                teleportAsync = Player.class.getMethod("teleportAsync", Location.class);

            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                folia = false;
            }
            Bukkit.getLogger().info("[CatSeedLogin] folia support loaded");
        }
    }

    // just teleport (for folia support)
    public static void teleport(Player player, Location location){
        if (folia) {
            try {
                teleportAsync.invoke(player,location);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else player.teleport(location);
    }


    // for cc.baka9.catseedlogin.bukkit.CatSeedLogin#runTaskAsync
    public static void runTaskAsync(Runnable runnable){
        if (folia){
            try {
                runNow.invoke(asyncScheduler,CatSeedLogin.instance, (Consumer<?>) task -> runnable.run());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(CatSeedLogin.instance, runnable);
        }
    }

    // from cc.baka9.catseedlogin.bukkit.task.Task
    private static final List<BukkitTask> bukkitTaskList = new ArrayList<>();

    // not type-safe
    private static final List<Object> foliaTaskList = new ArrayList<>();

    // for cc.baka9.catseedlogin.bukkit.task.Task#runTaskTimer
    public static void runTaskTimer(Runnable runnable, long l){
        if (folia){
            try {
                foliaTaskList.add(runAtFixedRate.invoke(globalRegionScheduler,CatSeedLogin.instance, (Consumer<?>) task -> runnable.run(), 1, l));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            bukkitTaskList.add(Bukkit.getScheduler().runTaskTimer(CatSeedLogin.instance, runnable, 0, l));
        }
    }

    // for cc.baka9.catseedlogin.bukkit.task.Task#cancelAll
    public static void cancelAll(){
        if(folia){
            Iterator<Object> iterator = foliaTaskList.iterator();
            while (iterator.hasNext()) {
                Object task = iterator.next();
                try {
                    cancel.invoke(scheduledTask.cast(task));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                iterator.remove();
            }
        } else {
            Iterator<BukkitTask> iterator = bukkitTaskList.iterator();
            while (iterator.hasNext()) {
                iterator.next().cancel();
                iterator.remove();
            }
        }
    }

    // for all codes that use org.bukkit.scheduler.BukkitScheduler#runTask
    public static void runTask(Runnable runnable){
        if (folia){
            try {
                run.invoke(globalRegionScheduler,CatSeedLogin.instance, (Consumer<?>) task -> runnable.run());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            Bukkit.getScheduler().runTask(CatSeedLogin.instance, runnable);
        }
    }

    // for all codes that use org.bukkit.scheduler.BukkitScheduler#runTaskLater
    public static void runTaskLater(Runnable runnable, long l){
        if (folia) {
            try {
                runDelayed.invoke(globalRegionScheduler,CatSeedLogin.instance, (Consumer<?>) task -> runnable.run(), l);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(CatSeedLogin.instance, runnable, l);
        }
    }
}
