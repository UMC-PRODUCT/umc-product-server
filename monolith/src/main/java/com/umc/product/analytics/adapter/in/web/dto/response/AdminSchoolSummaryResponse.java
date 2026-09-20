package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;

public record AdminSchoolSummaryResponse(
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName,
    long activeChallengerCount,
    StaffResponse president,
    StaffResponse vicePresident,
    PartLeaderRatioResponse partLeaderRatio,
    double averagePointSum,
    long riskChallengerCount,
    long newMemberCountThisWeek
) {

    public static AdminSchoolSummaryResponse from(AdminSchoolSummaryInfo info) {
        return new AdminSchoolSummaryResponse(
            info.schoolId(),
            info.schoolName(),
            info.chapterId(),
            info.chapterName(),
            info.activeChallengerCount(),
            StaffResponse.from(info.president()),
            StaffResponse.from(info.vicePresident()),
            PartLeaderRatioResponse.from(info.partLeaderRatio()),
            info.averagePointSum(),
            info.riskChallengerCount(),
            info.newMemberCountThisWeek()
        );
    }

    public record StaffResponse(Long challengerId, String name) {

        public static StaffResponse from(AdminSchoolSummaryInfo.StaffInfo info) {
            return info == null ? null : new StaffResponse(info.challengerId(), info.name());
        }
    }

    public record PartLeaderRatioResponse(long assigned, long totalRunningParts) {

        public static PartLeaderRatioResponse from(AdminSchoolSummaryInfo.PartLeaderRatioInfo info) {
            return new PartLeaderRatioResponse(info.assigned(), info.totalRunningParts());
        }
    }
}
