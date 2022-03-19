/*
 * Copyright (c) 2022, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package test.org.jfxcore.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
class ConcurrentTestBase {

    private final static int MAX_RETRY = 4;
    private final static long DEFAULT_TIMEOUT_MILLIS = 10000;

    private final ExecutorService executorService =
        Executors.newSingleThreadExecutor(r -> new Thread(r, "FX Test Thread"));

    private int invocation;
    private long deadline;

    @BeforeEach
    void setupEach() {
        setTimeout(DEFAULT_TIMEOUT_MILLIS);
    }

    public void retry(Runnable runnable) {
        for (int i = 0; true; ++i) {
            try {
                invocation = i;
                runnable.run();
                return;
            } catch (AssertionError ex) {
                if (i == MAX_RETRY - 1) {
                    throw ex;
                } else {
                    System.err.println(
                        runnable.getClass().getName() + " FAILED, retrying with relaxed timings");
                }
            }
        }
    }

    public void setTimeout(long millis) {
        deadline = System.currentTimeMillis() + millis;
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis * (long)Math.pow(2, invocation));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void runNow(Runnable runnable) {
        if (System.currentTimeMillis() > deadline) {
            fail("Test timed out.");
        }

        Throwable[] exception = new Throwable[1];
        var countDownLatch = new CountDownLatch(1);

        executorService.submit(() -> {
            try {
                runnable.run();
            } catch (Throwable ex) {
                exception[0] = ex;
            }

            countDownLatch.countDown();
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            sneakyThrow(ex);
        }

        if (exception[0] != null) {
            sneakyThrow(exception[0]);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T runNow(Supplier<T> supplier) {
        Object[] value = new Object[1];
        runNow((Runnable)() -> value[0] = supplier.get());
        return (T)value[0];
    }

    public void runLater(Runnable runnable) {
        executorService.submit(runnable);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E)e;
    }

}
