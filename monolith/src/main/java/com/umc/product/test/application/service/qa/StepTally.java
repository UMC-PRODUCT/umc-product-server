package com.umc.product.test.application.service.qa;

import java.util.HashMap;
import java.util.Map;

/**
 * 단계 하나가 만든 수, 이미 있어 건너뛴 수, 에러 코드별 개별 실패 수.
 */
final class StepTally {
    private int created;
    private int skipped;
    private final Map<String, Integer> failed = new HashMap<>();

    void created() {
        created++;
    }

    void created(int count) {
        created += count;
    }

    void skipped() {
        skipped++;
    }

    void skipped(int count) {
        skipped += count;
    }

    void failed(String code) {
        failed.merge(code, 1, Integer::sum);
    }

    int createdCount() {
        return created;
    }

    int skippedCount() {
        return skipped;
    }

    Map<String, Integer> failedByCode() {
        return Map.copyOf(failed);
    }
}
