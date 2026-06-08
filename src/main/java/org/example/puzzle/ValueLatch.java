package org.example.puzzle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

final class ValueLatch<T> {
    private final CountDownLatch done = new CountDownLatch(1);
    private final AtomicReference<T> value = new AtomicReference<>();

    boolean isSet() {
        return done.getCount() == 0;
    }

    boolean setValue(T newValue) {
        if (value.compareAndSet(null, newValue)) {
            done.countDown();
            return true;
        }
        return false;
    }

    T getValue() throws InterruptedException {
        done.await();
        return value.get();
    }

    T getValue(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (!done.await(timeout, unit)) {
            throw new TimeoutException("Timed out waiting for value");
        }
        return value.get();
    }
}
