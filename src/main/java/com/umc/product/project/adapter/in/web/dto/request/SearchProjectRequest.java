package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * 프로젝트 목록 검색 요청 (PROJECT-001).
 * <p>
 * statuses 의 자유도는 Service 레벨의 {@link com.umc.product.project.application.access.ProjectAccessScope}
 * 가 결정한다. 일반 챌린저가 statuses 를 자유 입력해도 PublicOnly scope 에서 IN_PROGRESS 로 덮어쓰여진다.
 */
public record SearchProjectRequest(
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    String keyword,

    Long chapterId,

    List<Long> schoolIds,

    List<ChallengerPart> parts,

    PartQuotaStatus partQuotaStatus,

    List<ProjectStatus> statuses
) {
    public SearchProjectQuery toQuery(Pageable pageable) {
        List<ProjectStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
            ? List.of(ProjectStatus.IN_PROGRESS)
            : statuses;
        return SearchProjectQuery.builder()
            .gisuId(gisuId)
            .keyword(keyword)
            .chapterId(chapterId)
            .schoolIds(schoolIds)
            .parts(parts)
            .partQuotaStatus(partQuotaStatus)
            .statuses(effectiveStatuses)
            .pageable(pageable)
            .build();
    }
}
