package org.example.puzzle;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueLatchTest {

    @Test
    void getValueWaitsUntilValueIsSet() throws Exception {
        ValueLatch<String> latch = new ValueLatch<>();

        try (var executor = Executors.newSingleThreadExecutor()) {
            FutureTask<String> future = new FutureTask<>(latch::getValue);
            executor.execute(future);

            assertFalse(future.isDone());
            latch.setValue("ready");

            assertEquals("ready", future.get(1, TimeUnit.SECONDS));
        }
    }

    @Test
    void setValuePublishesOnlyTheFirstValue() throws Exception {
        ValueLatch<String> latch = new ValueLatch<>();

        assertTrue(latch.setValue("first"));
        assertFalse(latch.setValue("second"));
        assertEquals("first", latch.getValue());
    }

    @Test
    void getValueWithTimeoutThrowsWhenValueIsNotSetInTime() {
        ValueLatch<String> latch = new ValueLatch<>();

        assertThrows(TimeoutException.class, () -> latch.getValue(50, TimeUnit.MILLISECONDS));
    }

    @Test
    void getValueWithTimeoutReturnsValueWhenItArrivesInTime() throws Exception {
        ValueLatch<String> latch = new ValueLatch<>();

        try (var executor = Executors.newSingleThreadExecutor()) {
            Runnable setterTask = () -> {
                try {
                    Thread.sleep(25);
                    latch.setValue("ready");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            };
            FutureTask<Void> setter = new FutureTask<>(setterTask, null);
            executor.execute(setter);

            assertEquals("ready", latch.getValue(1, TimeUnit.SECONDS));
            setter.get(1, TimeUnit.SECONDS);
        }
    }
}
