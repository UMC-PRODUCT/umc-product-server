package com.umc.product.test.application.service.qa;

import java.util.HashMap;
import java.util.Map;

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
