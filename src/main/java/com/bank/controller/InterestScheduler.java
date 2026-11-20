package com.bank.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//The "System Scheduler" actor: starts monthly interest automatically on the first day of each month.
public class InterestScheduler {
    private final InterestController interestController;
    private ScheduledExecutorService executor;

    public InterestScheduler(InterestController interestController) {
        this.interestController = interestController;
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "interest-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(this::runIfDue, 1, 60, TimeUnit.MINUTES);
    }

    void runIfDue() {
        if (LocalDate.now().getDayOfMonth() == 1) {
            try {
                interestController.runMonthlyInterest(YearMonth.now());
            } catch (RuntimeException e) {
                System.err.println("Scheduled interest run failed: " + e.getMessage());
            }
        }
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
