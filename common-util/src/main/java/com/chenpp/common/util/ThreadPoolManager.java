package com.chenpp.common.util;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池统一管理
 *
 * @author April.Chen
 * @date 2024/4/17 16:27
 */
public class ThreadPoolManager {

    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int DEFAULT_QUEUE_SIZE = 1024;
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;

    private static final Map<String, ThreadPoolExecutor> THREAD_POOL_MAP = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledExecutorService> SCHEDULED_THREAD_POOL_MAP = new ConcurrentHashMap<>();

    private ThreadPoolManager() {
    }

    /**
     * 新建线程池
     *
     * @param name             线程池名称
     * @param coreSize         核心线程数
     * @param maxSize          最大线程数
     * @param keepAliveSeconds 线程存活时间
     * @param queueSize        等待队列大小
     * @return 线程池
     */
    public static ThreadPoolExecutor newThreadPool(String name, int coreSize, int maxSize, long keepAliveSeconds, int queueSize) {
        if (THREAD_POOL_MAP.containsKey(name)) {
            return THREAD_POOL_MAP.get(name);
        }
        ThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern(name).build();
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueSize);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, maxSize, keepAliveSeconds, TimeUnit.SECONDS, workQueue, threadFactory);
        THREAD_POOL_MAP.put(name, executor);
        return executor;
    }

    public static ThreadPoolExecutor newThreadPool(String name) {
        return newThreadPool(name, DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE, DEFAULT_KEEP_ALIVE_SECONDS, DEFAULT_QUEUE_SIZE);
    }

    public static ThreadPoolExecutor newThreadPool(String name, int coreSize) {
        return newThreadPool(name, coreSize, coreSize, DEFAULT_KEEP_ALIVE_SECONDS, DEFAULT_QUEUE_SIZE);
    }

    /**
     * 新建定时线程池
     *
     * @param name     线程池名称
     * @param coreSize 线程池核心线程数
     * @return ScheduledExecutorService
     */
    public static ScheduledExecutorService newScheduledThreadPool(String name, int coreSize) {
        if (SCHEDULED_THREAD_POOL_MAP.containsKey(name)) {
            return SCHEDULED_THREAD_POOL_MAP.get(name);
        }
        ThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern(name).build();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(coreSize, threadFactory);
        SCHEDULED_THREAD_POOL_MAP.put(name, executor);
        return executor;
    }

    /**
     * 获取线程池
     *
     * @param name 线程池名称
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getThreadPool(String name) {
        return THREAD_POOL_MAP.get(name);
    }

    /**
     * 销毁线程池
     *
     * @param name 线程池名称
     */
    public static void destroyThreadPool(String name) {
        ThreadPoolExecutor executor = THREAD_POOL_MAP.get(name);
        if (executor != null) {
            executor.shutdown();
            THREAD_POOL_MAP.remove(name);
        }
    }

    /**
     * 获取调度线程池
     *
     * @param name 调度线程池名称
     * @return ScheduledExecutorService
     */
    public static ScheduledExecutorService getScheduledThreadPool(String name) {
        return SCHEDULED_THREAD_POOL_MAP.get(name);
    }

    /**
     * 销毁调度线程池
     *
     * @param name 调度线程池名称
     */
    public static void destroyScheduledThreadPool(String name) {
        ScheduledExecutorService executor = SCHEDULED_THREAD_POOL_MAP.get(name);
        if (executor != null) {
            executor.shutdown();
            SCHEDULED_THREAD_POOL_MAP.remove(name);
        }
    }

    public static Set<String> listThreadPoolNames() {
        return THREAD_POOL_MAP.keySet();
    }

    public static void shutdownAll() {
        THREAD_POOL_MAP.forEach((k, v) -> v.shutdown());
        SCHEDULED_THREAD_POOL_MAP.forEach((k, v) -> v.shutdown());
    }
}
