package net.kaupenjoe.mccourse.util;

import net.kaupenjoe.mccourse.MCCourseMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能监控器
 * 用于监控武器特效系统的性能表现
 */
public class PerformanceMonitor {
    private static PerformanceMonitor instance;
    
    // 性能计数器
    private final Map<String, Long> counters = new ConcurrentHashMap<>();
    
    // 性能计时器
    private final Map<String, Long> timers = new ConcurrentHashMap<>();
    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    
    // 性能统计
    private final Map<String, PerformanceStats> stats = new ConcurrentHashMap<>();
    
    // 配置参数
    private boolean enableProfiling = false;
    private long lastCleanupTime = 0;
    private final long CLEANUP_INTERVAL = 60000; // 1分钟

    private PerformanceMonitor() {}

    public static PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }

    /**
     * 性能统计数据类
     */
    public static class PerformanceStats {
        public long totalTime = 0;
        public long callCount = 0;
        public long maxTime = 0;
        public long minTime = Long.MAX_VALUE;

        public double getAverageTime() {
            return callCount > 0 ? (double) totalTime / callCount : 0;
        }
    }

    /**
     * 开始计时
     */
    public void startTimer(String name) {
        if (!enableProfiling) return;
        startTimes.put(name, System.nanoTime());
    }

    /**
     * 结束计时并记录
     */
    public void endTimer(String name) {
        if (!enableProfiling) return;
        
        Long startTime = startTimes.remove(name);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;
            recordTiming(name, duration);
        }
    }

    /**
     * 记录计时结果
     */
    private void recordTiming(String name, long duration) {
        PerformanceStats stat = stats.computeIfAbsent(name, k -> new PerformanceStats());
        
        stat.totalTime += duration;
        stat.callCount++;
        stat.maxTime = Math.max(stat.maxTime, duration);
        stat.minTime = Math.min(stat.minTime, duration);
    }

    /**
     * 增加计数器
     */
    public void incrementCounter(String name) {
        counters.merge(name, 1L, Long::sum);
    }

    /**
     * 增加计数器指定数量
     */
    public void incrementCounter(String name, long amount) {
        counters.merge(name, amount, Long::sum);
    }

    /**
     * 获取计数器值
     */
    public long getCounter(String name) {
        return counters.getOrDefault(name, 0L);
    }

    /**
     * 获取性能统计
     */
    public PerformanceStats getStats(String name) {
        return stats.get(name);
    }

    /**
     * 启用/禁用性能分析
     */
    public void setProfilingEnabled(boolean enabled) {
        this.enableProfiling = enabled;
        if (enabled) {
            MCCourseMod.LOGGER.info("Performance profiling enabled for weapon effects");
        } else {
            MCCourseMod.LOGGER.info("Performance profiling disabled");
        }
    }

    /**
     * 获取内存使用情况
     */
    @OnlyIn(Dist.CLIENT)
    public MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        return new MemoryInfo(totalMemory, freeMemory, usedMemory, maxMemory);
    }

    /**
     * 内存信息类
     */
    public static class MemoryInfo {
        public final long totalMemory;
        public final long freeMemory;
        public final long usedMemory;
        public final long maxMemory;

        public MemoryInfo(long totalMemory, long freeMemory, long usedMemory, long maxMemory) {
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
        }

        public double getUsagePercentage() {
            return (double) usedMemory / maxMemory * 100;
        }
    }

    /**
     * 检查性能警告
     */
    public void checkPerformanceWarnings() {
        if (!enableProfiling) return;

        // 检查是否需要清理旧数据
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL) {
            cleanup();
            lastCleanupTime = currentTime;
        }

        // 检查性能问题
        for (Map.Entry<String, PerformanceStats> entry : stats.entrySet()) {
            String operation = entry.getKey();
            PerformanceStats stat = entry.getValue();

            // 如果平均执行时间过长，发出警告
            double avgTimeMs = stat.getAverageTime() / 1_000_000.0; // 转换为毫秒
            if (avgTimeMs > 10.0) { // 超过10ms
                MCCourseMod.LOGGER.warn("Performance warning: {} average time: {:.2f}ms (calls: {})",
                    operation, avgTimeMs, stat.callCount);
            }

            // 如果最大执行时间过长，发出警告
            double maxTimeMs = stat.maxTime / 1_000_000.0;
            if (maxTimeMs > 50.0) { // 超过50ms
                MCCourseMod.LOGGER.warn("Performance warning: {} max time: {:.2f}ms", operation, maxTimeMs);
            }
        }

        // 检查内存使用
        if (Minecraft.getInstance() != null) {
            MemoryInfo memInfo = getMemoryInfo();
            if (memInfo.getUsagePercentage() > 85.0) {
                MCCourseMod.LOGGER.warn("High memory usage: {:.1f}%", memInfo.getUsagePercentage());
            }
        }
    }

    /**
     * 清理旧的性能数据
     */
    private void cleanup() {
        // 保留最近的统计数据，清理过旧的数据
        if (stats.size() > 100) {
            stats.clear();
            MCCourseMod.LOGGER.debug("Cleaned up performance statistics");
        }
        
        if (counters.size() > 50) {
            counters.clear();
            MCCourseMod.LOGGER.debug("Cleaned up performance counters");
        }
    }

    /**
     * 打印性能报告
     */
    public void printPerformanceReport() {
        if (!enableProfiling || stats.isEmpty()) {
            MCCourseMod.LOGGER.info("No performance data available");
            return;
        }

        MCCourseMod.LOGGER.info("=== Weapon Effects Performance Report ===");
        
        for (Map.Entry<String, PerformanceStats> entry : stats.entrySet()) {
            String operation = entry.getKey();
            PerformanceStats stat = entry.getValue();
            
            MCCourseMod.LOGGER.info("{}: avg={:.2f}ms, max={:.2f}ms, min={:.2f}ms, calls={}",
                operation,
                stat.getAverageTime() / 1_000_000.0,
                stat.maxTime / 1_000_000.0,
                stat.minTime / 1_000_000.0,
                stat.callCount
            );
        }

        if (!counters.isEmpty()) {
            MCCourseMod.LOGGER.info("=== Counters ===");
            for (Map.Entry<String, Long> entry : counters.entrySet()) {
                MCCourseMod.LOGGER.info("{}: {}", entry.getKey(), entry.getValue());
            }
        }

        if (Minecraft.getInstance() != null) {
            MemoryInfo memInfo = getMemoryInfo();
            MCCourseMod.LOGGER.info("Memory usage: {:.1f}% ({}/{} MB)",
                memInfo.getUsagePercentage(),
                memInfo.usedMemory / (1024 * 1024),
                memInfo.maxMemory / (1024 * 1024)
            );
        }
    }

    /**
     * 重置所有统计数据
     */
    public void reset() {
        stats.clear();
        counters.clear();
        timers.clear();
        startTimes.clear();
        MCCourseMod.LOGGER.info("Performance statistics reset");
    }
}