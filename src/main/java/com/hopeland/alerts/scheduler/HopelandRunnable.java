package com.hopeland.alerts.scheduler;

import com.hopeland.alerts.AlertsSystem;

public abstract class HopelandRunnable implements Runnable {

    private Scheduler.Task task;

    public HopelandRunnable() {
    }

    public synchronized Scheduler.Task runTask() {
        return this.task = AlertsSystem.getInstance().getScheduler().runTask(this);
    }

    public synchronized Scheduler.Task runTaskLater(long delay) {
        return this.task = AlertsSystem.getInstance().getScheduler().runTaskLater(this, delay);
    }

    public synchronized Scheduler.Task runTaskTimer(long delay, long period) {
        return this.task = AlertsSystem.getInstance().getScheduler().runTaskTimer(this, delay, period);
    }

    public synchronized void cancel() throws IllegalStateException {
        AlertsSystem.getInstance().getScheduler().cancelTask(this.task.getTaskId());
    }
}
