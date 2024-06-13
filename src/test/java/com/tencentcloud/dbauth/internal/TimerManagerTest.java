package com.tencentcloud.dbauth.internal;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TimerManagerTest {

    @Test
    public void testTimerManager() {
        AtomicInteger counter = new AtomicInteger(0);
        TimerManager timerManager = new TimerManager();
        timerManager.saveTimer("key", 100, () -> {
            counter.incrementAndGet();
            timerManager.saveTimer("key", 100, () -> {
                counter.incrementAndGet();
            });
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(2, counter.get());
    }
}
