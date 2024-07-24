package cc.baka9.catseedlogin.bukkit.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import space.arim.morepaperlib.scheduling.ScheduledTask;

public abstract class Task implements Runnable {
    private static List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private static CatSeedLogin plugin = CatSeedLogin.instance;
    protected Task(){
    }

    private static TaskAutoKick taskAutoKick;
    private static TaskSendLoginMessage taskSendLoginMessage;

    public static TaskAutoKick getTaskAutoKick(){
        if (taskAutoKick == null) {
            taskAutoKick = new TaskAutoKick();
        }
        return taskAutoKick;

    }

    public static TaskSendLoginMessage getTaskSendLoginMessage(){
        if (taskSendLoginMessage == null) {
            taskSendLoginMessage = new TaskSendLoginMessage();
        }
        return taskSendLoginMessage;

    }


    public static void runAll(){
        runTaskTimer(Task.getTaskSendLoginMessage(), 20 * 5);
        runTaskTimer(Task.getTaskAutoKick(), 20 * 5);

    }

    public static void cancelAll(){
        Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
        while (iterator.hasNext()) {
            iterator.next().cancel();
            iterator.remove();
        }

    }

    public static void runTaskTimer(Runnable runnable, long l){
        scheduledTasks.add(CatScheduler.runTaskTimer(runnable, 0, l));

    }
}
