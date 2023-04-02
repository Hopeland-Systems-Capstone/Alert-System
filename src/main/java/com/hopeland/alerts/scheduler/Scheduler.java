package com.hopeland.alerts.scheduler;

import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Scheduler {

    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(0);

    public Task runTask(Runnable runnable) {
        return runTask(new Task(runnable));
    }

    public Task runTaskLater(Runnable runnable, long delay) {
        return runTask(new Task(runnable, delay));
    }

    public Task runTaskTimer(Runnable runnable, long delay, long period) {
        return runTask(new Task(runnable, delay, period));
    }

    private Task runTask(Task task) {
        long taskId = taskIdGenerator.incrementAndGet();
        task.setTaskId(taskId);
        tasks.put(taskId, task);
        task.schedule(taskId);
        return task;
    }

    public void cancelTask(long taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    public class Task {
        private final long delay;
        private final long period;
        private final Runnable runnable;
        private Timer timer;
        private TimerTask timerTask;
        @Getter private boolean isCancelled;
        @Getter @Setter private long taskId;

        public Task(Runnable runnable) {
            this.runnable = runnable;
            this.delay = 0;
            this.period = 0;
        }

        public Task(Runnable runnable, long delay) {
            this.runnable = runnable;
            this.delay = delay;
            this.period = 0;
        }

        public Task(Runnable runnable, long delay, long period) {
            this.runnable = runnable;
            this.delay = delay;
            this.period = period;
        }

        public void schedule(long taskId) {
            if (delay == 0 && period == 0) {
                // run once
                execute(taskId);
            } else if (delay > 0 && period == 0) {
                // run once after a delay
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        execute(taskId);
                    }
                };
                timer.schedule(timerTask, delay);
            } else if (delay > 0 && period > 0) {
                // run repeatedly after a delay
                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        execute(taskId);
                    }
                };
                timer.schedule(timerTask, delay, period);
            }
        }

        private void execute(long taskId) {
            if (!isCancelled) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (period == 0) {
                // remove one-time task from the task list
                tasks.remove(taskId);
            }
        }

        public void cancel() {
            isCancelled = true;
            if (timerTask != null) {
                timerTask.cancel();
            }
            if (timer != null) {
                timer.cancel();
            }
            // remove the task here since it's safe to do so now
            tasks.values().remove(this);
        }
    }
}