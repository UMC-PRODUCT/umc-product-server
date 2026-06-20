package com.umc.product.project.adapter.in.web.dto.response.statistics;

import java.util.List;

import com.umc.product.project.adapter.in.web.dto.response.statistics.ProjectStatisticsResponse.ProjectMatchingRoundStatisticsResponse;
import com.umc.product.project.application.port.in.query.dto.statistics.ChapterProjectMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingCountInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolMatchingStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.UnclassifiedMatchingStatisticsInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지부별 공개 프로젝트 매칭 요약 응답")
public record ChapterProjectMatchingStatisticsResponse(
    @Schema(description = "지부 ID")
    Long chapterId,
    @Schema(description = "매칭 차수별 ProjectMember 기준 매칭 현황")
    List<RoundMatchingStatisticsResponse> roundMatchingStatistics,
    @Schema(description = "학교별 매칭 인원 수와 총원")
    List<SchoolMatchingStatisticsResponse> schoolMatchingStatistics,
    @Schema(description = "합격 지원서가 없어 매칭 차수에 귀속할 수 없는 ProjectMember 통계")
    UnclassifiedMatchingStatisticsResponse unclassifiedMatchingStatistics
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
            UnclassifiedMatchingStatisticsResponse.from(info.unclassifiedMatchingStatistics())
        );
    }

    @Schema(description = "매칭 차수별 ProjectMember 기준 매칭 현황")
    public record RoundMatchingStatisticsResponse(
        @Schema(description = "매칭 차수")
        ProjectMatchingRoundStatisticsResponse matchingRound,
        @Schema(description = "매칭 완료 인원 수")
        long matchedMemberCount,
        @Schema(description = "지원 가능 인원 수")
        long availableMemberCount,
        @Schema(description = "프로젝트별 매칭 완료 인원 수")
        List<ProjectMatchingCountResponse> projects
    ) {
        private static RoundMatchingStatisticsResponse from(RoundMatchingStatisticsInfo info) {
            return new RoundMatchingStatisticsResponse(
                new ProjectMatchingRoundStatisticsResponse(
                    info.matchingRound().matchingRoundId(),
                    info.matchingRound().type(),
                    info.matchingRound().phase()
                ),
                info.matchedMemberCount(),
                info.availableMemberCount(),
                info.projects().stream()
                    .map(ProjectMatchingCountResponse::from)
                    .toList()
            );
        }
    }

    @Schema(description = "프로젝트별 매칭 완료 인원 수")
    public record ProjectMatchingCountResponse(
        @Schema(description = "프로젝트 ID")
        Long projectId,
        @Schema(description = "매칭 완료 인원 수")
        long matchedMemberCount
    ) {
        private static ProjectMatchingCountResponse from(ProjectMatchingCountInfo info) {
            return new ProjectMatchingCountResponse(info.projectId(), info.matchedMemberCount());
        }
    }

    @Schema(description = "학교별 매칭 인원 수와 총원")
    public record SchoolMatchingStatisticsResponse(
        @Schema(description = "학교 ID")
        Long schoolId,
        @Schema(description = "매칭 완료 인원 수")
        long matchedMemberCount,
        @Schema(description = "학교별 지원 가능 총원")
        long totalMemberCount
    ) {
        private static SchoolMatchingStatisticsResponse from(SchoolMatchingStatisticsInfo info) {
            return new SchoolMatchingStatisticsResponse(
                info.schoolId(),
                info.matchedMemberCount(),
                info.totalMemberCount()
            );
        }
    }

    @Schema(description = "합격 지원서가 없어 매칭 차수에 귀속할 수 없는 ProjectMember 통계")
    public record UnclassifiedMatchingStatisticsResponse(
        @Schema(description = "매칭 차수 미분류 인원 수")
        long matchedMemberCount,
        @Schema(description = "프로젝트별 매칭 차수 미분류 인원 수")
        List<ProjectMatchingCountResponse> projects
    ) {
        private static UnclassifiedMatchingStatisticsResponse from(UnclassifiedMatchingStatisticsInfo info) {
            return new UnclassifiedMatchingStatisticsResponse(
                info.matchedMemberCount(),
                info.projects().stream()
                    .map(ProjectMatchingCountResponse::from)
                    .toList()
            );
        }
    }
}
