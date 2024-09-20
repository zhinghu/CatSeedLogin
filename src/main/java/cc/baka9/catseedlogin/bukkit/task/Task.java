package cc.baka9.catseedlogin.bukkit.task;

import java.util.ArrayList;
import java.util.List;

import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import space.arim.morepaperlib.scheduling.ScheduledTask;

public abstract class Task implements Runnable {
    private static final List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private static final CatSeedLogin plugin = CatSeedLogin.instance;

    private static TaskAutoKick taskAutoKick;
    private static TaskSendLoginMessage taskSendLoginMessage;

    protected Task() {}

    public static TaskAutoKick getTaskAutoKick() {
        if (taskAutoKick == null) {
            taskAutoKick = new TaskAutoKick();
        }
        return taskAutoKick;
    }

    public static TaskSendLoginMessage getTaskSendLoginMessage() {
        if (taskSendLoginMessage == null) {
            taskSendLoginMessage = new TaskSendLoginMessage();
        }
        return taskSendLoginMessage;
    }

    public static void runAll() {
        runTaskTimer(getTaskSendLoginMessage(), 20 * 5);
        runTaskTimer(getTaskAutoKick(), 20 * 5);
    }

    public static void cancelAll() {
        scheduledTasks.forEach(ScheduledTask::cancel);
        scheduledTasks.clear();
    }

    public static void runTaskTimer(Runnable runnable, long delay) {
        try {
            scheduledTasks.add(CatScheduler.runTaskTimer(runnable, 0, delay));
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }
}
