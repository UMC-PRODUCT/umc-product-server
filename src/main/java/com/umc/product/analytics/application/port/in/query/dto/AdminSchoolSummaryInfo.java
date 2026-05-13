package com.umc.product.analytics.application.port.in.query.dto;

public record AdminSchoolSummaryInfo(
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName,
    long activeChallengerCount,
    StaffInfo president,
    StaffInfo vicePresident,
    PartLeaderRatioInfo partLeaderRatio,
    double averagePointSum,
    long riskChallengerCount,
    long newMemberCountThisWeek
) {

    public static AdminSchoolSummaryInfo of(
        Long schoolId,
        String schoolName,
        Long chapterId,
        String chapterName,
        long activeChallengerCount,
        StaffInfo president,
        StaffInfo vicePresident,
        PartLeaderRatioInfo partLeaderRatio,
        double averagePointSum,
        long riskChallengerCount,
        long newMemberCountThisWeek
    ) {
        return new AdminSchoolSummaryInfo(
            schoolId,
            schoolName,
            chapterId,
            chapterName,
            activeChallengerCount,
            president,
            vicePresident,
            partLeaderRatio,
            averagePointSum,
            riskChallengerCount,
            newMemberCountThisWeek
        );
    }

    public record StaffInfo(Long challengerId, String name) {

        public static StaffInfo of(Long challengerId, String name) {
            return new StaffInfo(challengerId, name);
        }
    }

    public record PartLeaderRatioInfo(long assigned, long totalRunningParts) {

        public static PartLeaderRatioInfo of(long assigned, long totalRunningParts) {
            return new PartLeaderRatioInfo(assigned, totalRunningParts);
        }
    }
}
