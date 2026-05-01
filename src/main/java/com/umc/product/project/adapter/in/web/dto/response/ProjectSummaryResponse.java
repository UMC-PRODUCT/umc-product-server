package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.common.PartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import java.util.List;
import lombok.Builder;

/**
 * 프로젝트 목록 아이템 응답 (PROJECT-001).
 */
@Builder
public record ProjectSummaryResponse(
    Long id,
    String name,
    String description,
    String thumbnailImageUrl,
    MemberBrief productOwner,
    List<PartQuotaInfo> partQuotas,
    PartQuotaStatus partQuotaStatus
) {
    public static ProjectSummaryResponse from(ProjectInfo info, MemberBrief productOwner) {
        List<PartQuotaInfo> quotas = info.partQuotas().stream()
            .map(PartQuotaInfo::from)
            .toList();
        return ProjectSummaryResponse.builder()
            .id(info.id())
            .name(info.name())
            .description(info.description())
            .thumbnailImageUrl(info.thumbnailImageUrl())
            .productOwner(productOwner)
            .partQuotas(quotas)
            .partQuotaStatus(aggregateStatus(quotas))
            .build();
    }

    public ProjectSummaryResponse toPublic() {
        return ProjectSummaryResponse.builder()
            .id(id)
            .name(name)
            .description(description)
            .thumbnailImageUrl(thumbnailImageUrl)
            .productOwner(productOwner.toPublic())
            .partQuotas(partQuotas)
            .partQuotaStatus(partQuotaStatus)
            .build();
    }

    private static PartQuotaStatus aggregateStatus(List<PartQuotaInfo> quotas) {
        if (quotas.isEmpty()) return null;
        boolean anyRecruiting = quotas.stream()
            .anyMatch(q -> q.status() == PartQuotaStatus.RECRUITING);
        return anyRecruiting ? PartQuotaStatus.RECRUITING : PartQuotaStatus.COMPLETED;
    }
}
