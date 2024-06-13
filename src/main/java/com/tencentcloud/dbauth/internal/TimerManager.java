package com.tencentcloud.dbauth.internal;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimerManager is a utility class that provides methods for managing timer tasks.
 */
public final class TimerManager {
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    // A concurrent hash map to store ScheduledFuture objects associated with a key
    private final ConcurrentHashMap<String, ScheduledFuture<?>> timerMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.
            newScheduledThreadPool(CORE_POOL_SIZE, new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true); // Set the thread as a daemon thread
                    thread.setName("ScheduledThread-" + counter.incrementAndGet());
                    return thread;
                }
            });

    /**
     * Saves a timer task that runs after a specified interval.
     *
     * @param key   the key associated with the timer
     * @param delay the time in milliseconds to delay before the task is to be executed
     * @param task  the task to run
     */
    public void saveTimer(String key, long delay, Runnable task) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (delay <= 0 || delay > Constants.MAX_DELAY) {
            return;
        }

        synchronized (this) {
            ScheduledFuture<?> newFuture = executor.schedule(task, delay, TimeUnit.MILLISECONDS);

            // If a timer with the same key exists, cancel it and remove it from the map
            ScheduledFuture<?> future = timerMap.remove(key);
            if (future != null) {
                future.cancel(true);
            }

            timerMap.put(key, newFuture);
        }
    }
}