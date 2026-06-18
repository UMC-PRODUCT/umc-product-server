package com.umc.product.project.adapter.in.web.dto.response.statistics;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMatchingRoundStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectMemberStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.RoundSchoolApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.SchoolApplicationStatisticsInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 지원/매칭 현황 응답")
public record ProjectStatisticsResponse(
    @Schema(description = "프로젝트 ID")
    Long projectId,
    @Schema(description = "프로젝트에 최종 합류한 활성 멤버 목록")
    List<ProjectMemberStatisticsResponse> projectMembers,
    @Schema(description = "매칭 차수별 지원 완료 인원 수와 지원 가능 인원 수")
    List<RoundApplicationStatisticsResponse> roundApplicationStatistics,
    @Schema(description = "매칭 차수별 지원자 학교 인원 수")
    List<RoundSchoolApplicationStatisticsResponse> schoolApplicationStatistics
) implements ProjectStatisticsQueryResponse {
    public static ProjectStatisticsResponse from(ProjectStatisticsInfo info) {
        return new ProjectStatisticsResponse(
            info.projectId(),
            info.projectMembers().stream()
                .map(ProjectMemberStatisticsResponse::from)
                .toList(),
            info.roundApplicationStatistics().stream()
                .map(RoundApplicationStatisticsResponse::from)
                .toList(),
            info.schoolApplicationStatistics().stream()
                .map(RoundSchoolApplicationStatisticsResponse::from)
                .toList()
        );
    }

    @Schema(description = "프로젝트 멤버별 지원 이력")
    public record ProjectMemberStatisticsResponse(
        @Schema(description = "프로젝트 멤버 ID")
        Long projectMemberId,
        @Schema(description = "멤버 ID")
        Long memberId,
        @Schema(description = "프로젝트 내 파트")
        ChallengerPart part,
        @Schema(description = "프로젝트 멤버 상태")
        ProjectMemberStatus status,
        @Schema(description = "해당 멤버가 이 프로젝트에 작성한 지원 이력. 강제 배정이면 빈 목록")
        List<ProjectMemberApplicationStatisticsResponse> applications
    ) {
        private static ProjectMemberStatisticsResponse from(ProjectMemberStatisticsInfo info) {
            return new ProjectMemberStatisticsResponse(
                info.projectMemberId(),
                info.memberId(),
                info.part(),
                info.status(),
                info.applications().stream()
                    .map(ProjectMemberApplicationStatisticsResponse::from)
                    .toList()
            );
        }
    }

    @Schema(description = "프로젝트 멤버의 차수별 지원 이력")
    public record ProjectMemberApplicationStatisticsResponse(
        @Schema(description = "지원서 ID")
        Long applicationId,
        @Schema(description = "지원서 상태")
        ProjectApplicationStatus status,
        @Schema(description = "지원서가 연결된 매칭 차수")
        ProjectMatchingRoundStatisticsResponse matchingRound
    ) {
        private static ProjectMemberApplicationStatisticsResponse from(
            ProjectMemberApplicationStatisticsInfo info
        ) {
            return new ProjectMemberApplicationStatisticsResponse(
                info.applicationId(),
                info.status(),
                ProjectMatchingRoundStatisticsResponse.from(info.matchingRound())
            );
        }
    }

    @Schema(description = "지원서가 연결된 매칭 차수 정보")
    public record ProjectMatchingRoundStatisticsResponse(
        @Schema(description = "매칭 차수 ID")
        Long matchingRoundId,
        @Schema(description = "매칭 유형")
        MatchingType type,
        @Schema(description = "매칭 차수")
        MatchingPhase phase
    ) {
        private static ProjectMatchingRoundStatisticsResponse from(ProjectMatchingRoundStatisticsInfo info) {
            return new ProjectMatchingRoundStatisticsResponse(
                info.matchingRoundId(),
                info.type(),
                info.phase()
            );
        }
    }

    @Schema(description = "매칭 차수별 지원 완료 인원 수와 지원 가능 인원 수")
    public record RoundApplicationStatisticsResponse(
        @Schema(description = "매칭 차수")
        ProjectMatchingRoundStatisticsResponse matchingRound,
        @Schema(description = "지원 완료 인원 수")
        long appliedMemberCount,
        @Schema(description = "지원 가능 인원 수")
        long availableMemberCount
    ) {
        static RoundApplicationStatisticsResponse from(RoundApplicationStatisticsInfo info) {
            return new RoundApplicationStatisticsResponse(
                ProjectMatchingRoundStatisticsResponse.from(info.matchingRound()),
                info.appliedMemberCount(),
                info.availableMemberCount()
            );
        }
    }

    @Schema(description = "매칭 차수별 지원자 학교 인원 수")
    public record RoundSchoolApplicationStatisticsResponse(
        @Schema(description = "매칭 차수")
        ProjectMatchingRoundStatisticsResponse matchingRound,
        @Schema(description = "학교별 지원자 수")
        List<SchoolApplicationStatisticsResponse> schools
    ) {
        static RoundSchoolApplicationStatisticsResponse from(RoundSchoolApplicationStatisticsInfo info) {
            return new RoundSchoolApplicationStatisticsResponse(
                ProjectMatchingRoundStatisticsResponse.from(info.matchingRound()),
                info.schools().stream()
                    .map(SchoolApplicationStatisticsResponse::from)
                    .toList()
            );
        }
    }

    @Schema(description = "학교별 지원자 수")
    public record SchoolApplicationStatisticsResponse(
        @Schema(description = "학교 ID")
        Long schoolId,
        @Schema(description = "지원자 수")
        long applicantCount
    ) {
        static SchoolApplicationStatisticsResponse from(SchoolApplicationStatisticsInfo info) {
            return new SchoolApplicationStatisticsResponse(info.schoolId(), info.applicantCount());
        }
    }
}
