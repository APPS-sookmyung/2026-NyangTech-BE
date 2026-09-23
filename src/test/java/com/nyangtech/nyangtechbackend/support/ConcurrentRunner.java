package com.nyangtech.nyangtechbackend.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** 같은 작업을 여러 스레드가 "같은 순간에" 실행하게 하는 도우미. 각 작업의 결과(예외 포함)를 모아 돌려준다. */
public final class ConcurrentRunner {

    private ConcurrentRunner() {
    }

    public static List<Object> run(int threads, Callable<Object> task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Object>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        return task.call();
                    } catch (Exception e) {
                        return e;
                    }
                }));
            }
            ready.await();
            start.countDown();

            List<Object> results = new ArrayList<>();
            for (Future<Object> future : futures) {
                results.add(future.get(60, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    /** 결과 중 성공(예외가 아닌 것)의 개수 */
    public static long successes(List<Object> results) {
        return results.stream().filter(result -> !(result instanceof Exception)).count();
    }
}
