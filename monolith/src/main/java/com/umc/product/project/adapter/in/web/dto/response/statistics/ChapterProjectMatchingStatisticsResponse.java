package com.umc.product.project.adapter.in.web.dto.response.statistics;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.statistics.ChapterProjectMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo.PartMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingStatisticsInfo.PhaseMatchingCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolMatchingStatisticsInfo;
import com.umc.product.project.domain.enums.MatchingPhase;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지부별 공개 프로젝트 매칭 통계 응답")
public record ChapterProjectMatchingStatisticsResponse(
    @Schema(description = "지부 ID")
    Long chapterId,
    @Schema(description = "차수별 매칭 현황")
    List<RoundMatchingStatisticsResponse> roundMatchingStatistics,
    @Schema(description = "학교별 누적 매칭 인원과 매칭 대상 총원")
    List<SchoolMatchingStatisticsResponse> schoolMatchingStatistics,
    @Schema(description = "프로젝트별 매칭 현황")
    List<ProjectMatchingStatisticsResponse> projectMatchingStatistics
) {
    public static ChapterProjectMatchingStatisticsResponse from(ChapterProjectMatchingStatisticsInfo info) {
        return new ChapterProjectMatchingStatisticsResponse(
            info.chapterId(),
            info.roundMatchingStatistics().stream()
                .map(RoundMatchingStatisticsResponse::from)
                .toList(),
            info.schoolMatchingStatistics().stream()
                .map(SchoolMatchingStatisticsResponse::from)
                .toList(),
            info.projectMatchingStatistics().stream()
                .map(ProjectMatchingStatisticsResponse::from)
                .toList()
        );
    }

    @Schema(description = "차수별 매칭 현황")
    public record RoundMatchingStatisticsResponse(
        @Schema(description = "매칭 차수")
        MatchingPhase phase,
        @Schema(description = "매칭 완료 인원 수")
        long matchedMemberCount,
        @Schema(description = "매칭 대기 인원 수. 이전 차수까지 매칭된 인원을 뺀 값입니다.")
        long availableMemberCount
    ) {
        private static RoundMatchingStatisticsResponse from(RoundMatchingStatisticsInfo info) {
            return new RoundMatchingStatisticsResponse(
                info.phase(),
                info.matchedMemberCount(),
                info.availableMemberCount()
            );
        }
    }

    @Schema(description = "학교별 매칭 현황")
    public record SchoolMatchingStatisticsResponse(
        @Schema(description = "학교 ID")
        Long schoolId,
        @Schema(description = "학교명")
        String schoolName,
        @Schema(description = "누적 매칭 완료 인원 수")
        long matchedMemberCount,
        @Schema(description = "매칭 대상 총원. 차수가 지나도 차감하지 않습니다.")
        long totalMemberCount
    ) {
        private static SchoolMatchingStatisticsResponse from(SchoolMatchingStatisticsInfo info) {
            return new SchoolMatchingStatisticsResponse(
                info.schoolId(),
                info.schoolName(),
                info.matchedMemberCount(),
                info.totalMemberCount()
            );
        }
    }

    @Schema(description = "프로젝트별 매칭 현황")
    public record ProjectMatchingStatisticsResponse(
        @Schema(description = "프로젝트 ID")
        Long projectId,
        @Schema(description = "프로젝트명")
        String projectName,
        @Schema(description = "누적 매칭 완료 인원 수")
        long matchedMemberCount,
        @Schema(description = "기준 매칭 유형 파트의 모집 정원(TO) 합계")
        long quota,
        @Schema(description = "차수별 매칭 완료 인원 수")
        List<PhaseMatchingCountResponse> rounds,
        @Schema(description = "파트별 누적 매칭 완료 인원 수와 모집 정원")
        List<PartMatchingStatisticsResponse> parts
    ) {
        private static ProjectMatchingStatisticsResponse from(ProjectMatchingStatisticsInfo info) {
            return new ProjectMatchingStatisticsResponse(
                info.projectId(),
                info.projectName(),
                info.matchedMemberCount(),
                info.quota(),
                info.rounds().stream()
                    .map(PhaseMatchingCountResponse::from)
                    .toList(),
                info.parts().stream()
                    .map(PartMatchingStatisticsResponse::from)
                    .toList()
            );
        }
    }

    @Schema(description = "차수별 매칭 완료 인원 수")
    public record PhaseMatchingCountResponse(
        @Schema(description = "매칭 차수")
        MatchingPhase phase,
        @Schema(description = "매칭 완료 인원 수")
        long matchedMemberCount
    ) {
        private static PhaseMatchingCountResponse from(PhaseMatchingCountInfo info) {
            return new PhaseMatchingCountResponse(info.phase(), info.matchedMemberCount());
        }
    }

    @Schema(description = "파트별 누적 매칭 완료 인원 수와 모집 정원")
    public record PartMatchingStatisticsResponse(
        @Schema(description = "파트")
        ChallengerPart part,
        @Schema(description = "누적 매칭 완료 인원 수")
        long matchedMemberCount,
        @Schema(description = "모집 정원(TO)")
        long quota
    ) {
        private static PartMatchingStatisticsResponse from(PartMatchingStatisticsInfo info) {
            return new PartMatchingStatisticsResponse(
                info.part(),
                info.matchedMemberCount(),
                info.quota()
            );
        }
    }
}
