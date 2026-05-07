package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.common.PartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationViewStatus;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import java.util.List;
import lombok.Builder;

/**
 * 본인 지원 내역 카드 1건의 Web Response DTO.
 */
@Builder
public record MyProjectApplicationResponse(
    Long applicationId,
    Long projectId,
    ProjectBrief project,
    MatchingRoundBrief matchingRound,
    ProjectApplicationViewStatus status
) {
    public static MyProjectApplicationResponse from(MyProjectApplicationCardInfo info, MemberBrief productOwner) {
        List<PartQuotaInfo> quotas = info.partQuotas().stream()
            .map(PartQuotaInfo::from)
            .toList();

        ProjectBrief project = ProjectBrief.builder()
            .name(info.projectName())
            .thumbnailImageUrl(info.projectThumbnailImageUrl())
            .productOwner(productOwner)
            .partQuotas(quotas)
            .build();

        MatchingRoundBrief round = MatchingRoundBrief.builder()
            .id(info.matchingRoundId())
            .type(info.matchingRoundType())
            .phase(info.matchingRoundPhase())
            .build();

        return MyProjectApplicationResponse.builder()
            .applicationId(info.applicationId())
            .projectId(info.projectId())
            .project(project)
            .matchingRound(round)
            .status(info.status())
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
     */
    @Builder
    public record MatchingRoundBrief(
        Long id,
        MatchingType type,
        MatchingPhase phase
    ) {
    }
}
