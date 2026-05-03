package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.common.PartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import lombok.Builder;

/**
 * 관리 화면용 프로젝트 목록 아이템 응답 (PROJECT-006).
 * <p>
 * {@link ProjectSummaryResponse} + {@code status} 필드. 운영진/PM 이 본인 관할 프로젝트의 상태(PENDING_REVIEW/
 * IN_PROGRESS/COMPLETED/ABORTED)를 한 화면에서 확인하기 위함.
 */
@Builder
public record ManagedProjectSummaryResponse(
    Long id,
    String name,
    String description,
    String thumbnailImageUrl,
    ProjectStatus status,
    MemberBrief productOwner,
    List<PartQuotaInfo> partQuotas,
    PartQuotaStatus partQuotaStatus
) {
    public static ManagedProjectSummaryResponse from(ProjectInfo info, MemberBrief productOwner) {
        List<PartQuotaInfo> quotas = info.partQuotas().stream()
            .map(PartQuotaInfo::from)
            .toList();
        return ManagedProjectSummaryResponse.builder()
            .id(info.id())
            .name(info.name())
            .description(info.description())
            .thumbnailImageUrl(info.thumbnailImageUrl())
            .status(info.status())
            .productOwner(productOwner)
            .partQuotas(quotas)
            .partQuotaStatus(aggregateStatus(quotas))
            .build();
    }

    public ManagedProjectSummaryResponse toPublic() {
        return ManagedProjectSummaryResponse.builder()
            .id(id)
            .name(name)
            .description(description)
            .thumbnailImageUrl(thumbnailImageUrl)
            .status(status)
            .productOwner(productOwner == null ? null : productOwner.toPublic())
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
