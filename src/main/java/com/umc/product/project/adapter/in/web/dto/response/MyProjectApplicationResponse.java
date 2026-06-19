package com.umc.product.project.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.project.adapter.in.web.dto.common.MatchingRoundPhaseView;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.common.PartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationSummaryInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationViewStatus;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberInfo;
import com.umc.product.project.domain.enums.MatchingType;

import lombok.Builder;

/**
 * 본인 지원 내역 카드 1건의 Web Response DTO.
 *
 * @param applicationId 지원서 기반 카드만 값을 가지며 랜덤 매칭 카드는 {@code null}
 * @param status        표시용 지원 상태. 지원자 본인이 결과를 아직 확인할 수 없으면 {@code null}. 랜덤 매칭 카드는 {@code APPROVED} 로 고정 표시된다.
 */
@Builder
public record MyProjectApplicationResponse(
    Long applicationId,
    Long projectId,
    ProjectBrief project,
    MatchingRoundBrief matchingRound,
    ProjectApplicationViewStatus status
) {
    /**
     * 본인이 제출한 ProjectApplication 기반 카드.
     */
    public static MyProjectApplicationResponse fromApplication(
        ProjectApplicationSummaryInfo application,
        ProjectInfo project,
        ProjectMatchingRoundInfo round,
        MemberBrief productOwner
    ) {
        return MyProjectApplicationResponse.builder()
            .applicationId(application.id())
            .projectId(application.projectId())
            .project(toProjectBrief(project, productOwner))
            .matchingRound(MatchingRoundBrief.builder()
                .id(round == null ? null : round.id())
                .type(round == null ? null : round.type())
                .phase(round == null ? null : MatchingRoundPhaseView.from(round.phase()))
                .build())
            .status(application.status() == null ? null : ProjectApplicationViewStatus.from(application.status()))
            .build();
    }

    /**
     * 랜덤 매칭/운영진 강제 배정으로 합류한 ProjectMember 기반 카드.
     * <p>
     * applicationId/matchingRoundId 는 {@code null}, status 는 {@code APPROVED} 고정, phase 는 {@code RANDOM_MATCHING}, type
     * 은 본인 챌린저 파트로부터 추론된다.
     */
    public static MyProjectApplicationResponse fromRandomMatched(
        ProjectMemberInfo member,
        ProjectInfo project,
        MemberBrief productOwner
    ) {
        return MyProjectApplicationResponse.builder()
            .applicationId(null)
            .projectId(member.projectId())
            .project(toProjectBrief(project, productOwner))
            .matchingRound(MatchingRoundBrief.builder()
                .id(null)
                .type(MatchingType.fromPart(member.part()).orElse(null))
                .phase(MatchingRoundPhaseView.RANDOM_MATCHING)
                .build())
            .status(ProjectApplicationViewStatus.APPROVED)
            .build();
    }

    private static ProjectBrief toProjectBrief(ProjectInfo project, MemberBrief productOwner) {
        if (project == null) {
            return null;
        }
        List<PartQuotaInfo> quotas = project.partQuotas().stream()
            .map(PartQuotaInfo::from)
            .toList();
        return ProjectBrief.builder()
            .name(project.name())
            .thumbnailImageUrl(project.thumbnailImageUrl())
            .productOwner(productOwner)
            .partQuotas(quotas)
            .build();
    }

    /**
     * @param productOwner PM 의 닉네임/실명/학교 정보
     */
    @Builder
    public record ProjectBrief(
        String name,
        String thumbnailImageUrl,
        MemberBrief productOwner,
        List<PartQuotaInfo> partQuotas
    ) {
    }

    /**
     * 매칭 라운드 식별 정보. 라벨 합성(예: "기획-개발자 1차 매칭")은 클라이언트가 type/phase 조합으로 처리한다.
     *
     * @param id    지원서 기반 카드만 값을 가지며 랜덤 매칭 카드는 {@code null}
     * @param phase 도메인 enum 대신 사용하는 표시용 enum. 랜덤 매칭/운영진 강제 배정은 {@code RANDOM_MATCHING} 으로 표시된다.
     */
    @Builder
    public record MatchingRoundBrief(
        Long id,
        MatchingType type,
        MatchingRoundPhaseView phase
    ) {
    }
}
