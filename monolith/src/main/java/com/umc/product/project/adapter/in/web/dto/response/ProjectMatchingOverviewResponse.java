package com.umc.product.project.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewMemberInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewOwnerInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewPartInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewProjectInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo.MatchingOverviewRoundInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.PartQuotaStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProjectMatchingOverviewResponse", description = "지부별 프로젝트 매칭 현황 응답")
public record ProjectMatchingOverviewResponse(
    @Schema(description = "프로젝트별 매칭 현황")
    List<MatchingOverviewProjectResponse> projects
) {
    public static ProjectMatchingOverviewResponse from(ProjectMatchingOverviewInfo info) {
        return new ProjectMatchingOverviewResponse(
            info.projects().stream()
                .map(MatchingOverviewProjectResponse::from)
                .toList()
        );
    }

    @Schema(name = "ProjectMatchingOverviewProjectResponse", description = "프로젝트별 매칭 현황")
    public record MatchingOverviewProjectResponse(
        @Schema(description = "프로젝트 ID")
        Long projectId,
        @Schema(description = "프로젝트명")
        String projectName,
        @Schema(description = "디자인·개발 팀원 수 합계")
        long currentMemberCount,
        @Schema(description = "디자인·개발 모집 정원(TO) 합계")
        long quota,
        @Schema(description = "모집 상태. 정원이 남은 파트가 있으면 RECRUITING이며, 디자인·개발 TO가 없으면 null입니다.", nullable = true)
        PartQuotaStatus partQuotaStatus,
        @Schema(description = "담당 PM")
        MatchingOverviewOwnerResponse productOwner,
        @Schema(description = "보조 PM 목록")
        List<MatchingOverviewOwnerResponse> coProductOwners,
        @Schema(description = "파트별 팀원과 모집 정원")
        List<MatchingOverviewPartResponse> parts
    ) {
        private static MatchingOverviewProjectResponse from(MatchingOverviewProjectInfo info) {
            return new MatchingOverviewProjectResponse(
                info.projectId(),
                info.projectName(),
                info.currentMemberCount(),
                info.quota(),
                info.partQuotaStatus(),
                MatchingOverviewOwnerResponse.from(info.productOwner()),
                info.coProductOwners().stream()
                    .map(MatchingOverviewOwnerResponse::from)
                    .toList(),
                info.parts().stream()
                    .map(MatchingOverviewPartResponse::from)
                    .toList()
            );
        }
    }

    @Schema(name = "ProjectMatchingOverviewPartResponse", description = "파트별 팀원과 모집 정원")
    public record MatchingOverviewPartResponse(
        @Schema(description = "파트")
        ChallengerPart part,
        @Schema(description = "팀원 수")
        long currentMemberCount,
        @Schema(description = "모집 정원(TO)")
        long quota,
        @Schema(description = "모집 상태. 팀원 수가 정원보다 적으면 RECRUITING입니다.")
        PartQuotaStatus status,
        @Schema(description = "팀원 목록. 팀원이 없으면 빈 배열입니다.")
        List<MatchingOverviewMemberResponse> members
    ) {
        private static MatchingOverviewPartResponse from(MatchingOverviewPartInfo info) {
            return new MatchingOverviewPartResponse(
                info.part(),
                info.currentMemberCount(),
                info.quota(),
                info.status(),
                info.members().stream()
                    .map(MatchingOverviewMemberResponse::from)
                    .toList()
            );
        }
    }

    @Schema(name = "ProjectMatchingOverviewOwnerResponse", description = "PM 정보")
    public record MatchingOverviewOwnerResponse(
        @Schema(description = "회원 ID")
        Long memberId,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "이름")
        String name,
        @Schema(description = "학교명")
        String schoolName
    ) {
        private static MatchingOverviewOwnerResponse from(MatchingOverviewOwnerInfo info) {
            return new MatchingOverviewOwnerResponse(
                info.memberId(),
                info.nickname(),
                info.name(),
                info.schoolName()
            );
        }
    }

    @Schema(name = "ProjectMatchingOverviewMemberResponse", description = "팀원 정보")
    public record MatchingOverviewMemberResponse(
        @Schema(description = "회원 ID")
        Long memberId,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "이름")
        String name,
        @Schema(description = "학교명")
        String schoolName,
        @Schema(description = "배정된 매칭 차수. 차수 없이 수동 배정된 팀원은 null입니다.", nullable = true)
        MatchingOverviewRoundResponse matchedRoundInfo
    ) {
        private static MatchingOverviewMemberResponse from(MatchingOverviewMemberInfo info) {
            return new MatchingOverviewMemberResponse(
                info.memberId(),
                info.nickname(),
                info.name(),
                info.schoolName(),
                info.matchedRoundInfo() == null ? null
                    : MatchingOverviewRoundResponse.from(info.matchedRoundInfo())
            );
        }
    }

    @Schema(name = "ProjectMatchingOverviewRoundResponse", description = "배정된 매칭 차수")
    public record MatchingOverviewRoundResponse(
        @Schema(description = "매칭 차수 ID")
        Long matchingRoundId,
        @Schema(description = "매칭 유형")
        MatchingType type,
        @Schema(description = "매칭 차수")
        MatchingPhase phase
    ) {
        private static MatchingOverviewRoundResponse from(MatchingOverviewRoundInfo info) {
            return new MatchingOverviewRoundResponse(info.matchingRoundId(), info.type(), info.phase());
        }
    }
}
