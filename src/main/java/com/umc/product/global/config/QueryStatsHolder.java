package com.umc.product.global.config;

/**
 * HTTP 요청 단위로 쿼리 실행 횟수와 총 소요 시간을 추적하는 ThreadLocal 홀더
 */
public class QueryStatsHolder {

    private static final ThreadLocal<long[]> HOLDER = new ThreadLocal<>();
    // [0] = 쿼리 실행 횟수, [1] = 총 소요 시간 (ms)

    public static void init() {
        HOLDER.set(new long[]{0L, 0L});
    }

    public static void record(long elapsedNanos) {
        long[] stats = HOLDER.get();
        if (stats != null) {
            stats[0]++;
            stats[1] += elapsedNanos / 1_000_000L;
        }
    }

    public static long getQueryCount() {
        long[] stats = HOLDER.get();
        return stats != null ? stats[0] : 0L;
    }

    public static long getTotalTimeMs() {
        long[] stats = HOLDER.get();
        return stats != null ? stats[1] : 0L;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
