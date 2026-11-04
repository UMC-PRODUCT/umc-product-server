package com.umc.product.test.domain.rehearsal;

@FunctionalInterface
public interface MinimumRule {
    int minimumRequired(int applicants, int remainingQuota);
}
