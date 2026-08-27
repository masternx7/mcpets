package fr.nocsy.mcpets.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class PetTimer {

    @Getter
    private static Map<PetTimer, ScheduledTask> runningTimers = new ConcurrentHashMap<>();

    @Getter
    private int cooldown;
    @Getter
    private int remainingTime;
    private long frequency;

    private ScheduledTask task;

    private final Runnable endingRunnable;

    /**
     * Constructor
     * Frequency giving the tick when repeating the task
     */
    public PetTimer(int cooldown, long frequency, Runnable endingRunnable) {
        this.cooldown = cooldown;
        this.remainingTime = 0;
        this.frequency = frequency;
        this.endingRunnable = endingRunnable;
    }

    public void launch(Runnable runnable) {
        // If it's running then cancel the current scheduler
        if (isRunning())
            stop(null);
        remainingTime = cooldown;
        task = ServerTasks.runGlobalTimer(() -> {
            if (cooldown != Integer.MAX_VALUE)
                remainingTime--;
            if (remainingTime <= 0)
                stop(endingRunnable);

            if (runnable != null)
                runnable.run();
        }, 1L, frequency);
        if (task != null) {
            runningTimers.put(this, task);
        }
    }

    public void stop(Runnable runnable) {
        ServerTasks.cancel(task);
        task = null;
        runningTimers.remove(this);
        remainingTime = 0;
        if (runnable != null)
            runnable.run();
    }

    public boolean isRunning() {
        return remainingTime > 0;
    }
}
