package cc.baka9.catseedlogin.bukkit.task;

import cc.baka9.catseedlogin.bukkit.CatScheduler;

public abstract class Task implements Runnable {
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
        CatScheduler.cancelAll();

    }

    public static void runTaskTimer(Runnable runnable, long l){
        CatScheduler.runTaskTimer(runnable,l);

    }
}
