package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.common.PartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import java.util.List;
import lombok.Builder;

/**
 * 프로젝트 상세 응답 (PROJECT-002).
 * <p>
 * {@code applicationFormId} 가 null 이 아니면 PM이 PROJECT-106 으로 폼을 1회 이상 작성한 상태.
 * 폼 전체 구조는 별도 PROJECT-106-GET 호출로 조회한다.
 */
@Builder
public record ProjectDetailResponse(
    Long id,
    String name,
    String description,
    String thumbnailImageUrl,
    String logoImageUrl,
    String externalLink,
    MemberBrief productOwner,
    List<MemberBrief> coProductOwners,
    List<PartQuotaInfo> partQuotas,
    PartQuotaStatus partQuotaStatus,
    Long applicationFormId
) {
    public static ProjectDetailResponse from(
        ProjectInfo info,
        MemberBrief productOwner,
        List<MemberBrief> coProductOwners,
        Long applicationFormId
    ) {
        List<PartQuotaInfo> quotas = info.partQuotas().stream()
            .map(PartQuotaInfo::from)
            .toList();
        return ProjectDetailResponse.builder()
            .id(info.id())
            .name(info.name())
            .description(info.description())
            .thumbnailImageUrl(info.thumbnailImageUrl())
            .logoImageUrl(info.logoImageUrl())
            .externalLink(info.externalLink())
            .productOwner(productOwner)
            .coProductOwners(coProductOwners)
            .partQuotas(quotas)
            .partQuotaStatus(aggregateStatus(quotas))
            .applicationFormId(applicationFormId)
            .build();
    }

    public ProjectDetailResponse toPublic() {
        return ProjectDetailResponse.builder()
            .id(id)
            .name(name)
            .description(description)
            .thumbnailImageUrl(thumbnailImageUrl)
            .logoImageUrl(logoImageUrl)
            .externalLink(externalLink)
            .productOwner(productOwner.toPublic())
            .coProductOwners(coProductOwners.stream().map(MemberBrief::toPublic).toList())
            .partQuotas(partQuotas)
            .partQuotaStatus(partQuotaStatus)
            .applicationFormId(applicationFormId)
            .build();
    }

    private static PartQuotaStatus aggregateStatus(List<PartQuotaInfo> quotas) {
        if (quotas.isEmpty()) return null;
        boolean anyRecruiting = quotas.stream()
            .anyMatch(q -> q.status() == PartQuotaStatus.RECRUITING);
        return anyRecruiting ? PartQuotaStatus.RECRUITING : PartQuotaStatus.COMPLETED;
    }
}
