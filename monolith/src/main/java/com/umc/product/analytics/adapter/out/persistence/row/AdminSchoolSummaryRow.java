package com.umc.product.analytics.adapter.out.persistence.row;

public record AdminSchoolSummaryRow(
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName,
    long activeChallengerCount,
    double averagePointSum,
    long riskChallengerCount,
    long newMemberCountThisWeek,
    long totalRunningParts
) {
}
