package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

/**
 * 프로젝트 목록 검색 Query (PROJECT-001).
 * <p>
 * 권한(챌린저 vs Admin)에 따라 노출되는 상태 범위가 다릅니다.
 * 직접 {@code builder()}/생성자로 만들지 말고 {@link #forChallenger} 또는 {@link #forAdmin}을 사용하세요.
 * Controller는 분기 로직을 가지지 않고, Request DTO의 {@code toQuery(isAdmin, pageable)}에서 팩토리를 선택합니다.
 */
@Builder
public record SearchProjectQuery(
    Long gisuId,
    String keyword,
    Long chapterId,
    List<Long> schoolIds,
    List<ChallengerPart> parts,
    PartQuotaStatus partQuotaStatus,
    List<ProjectStatus> statuses,
    Pageable pageable
) {
    public SearchProjectQuery {
        Objects.requireNonNull(gisuId, "gisuId must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");
        if (statuses == null || statuses.isEmpty()) {
            throw new IllegalArgumentException("statuses must contain at least one ProjectStatus");
        }
    }

    /**
     * 일반 챌린저용 — {@code statuses}를 {@code [IN_PROGRESS]}로 강제합니다.
     * 챌린저 요청에서 넘어온 {@code statuses} 값은 무시됩니다.
     */
    public static SearchProjectQuery forChallenger(
        Long gisuId,
        String keyword,
        Long chapterId,
        List<Long> schoolIds,
        List<ChallengerPart> parts,
        PartQuotaStatus partQuotaStatus,
        Pageable pageable
    ) {
        return SearchProjectQuery.builder()
            .gisuId(gisuId)
            .keyword(keyword)
            .chapterId(chapterId)
            .schoolIds(schoolIds)
            .parts(parts)
            .partQuotaStatus(partQuotaStatus)
            .statuses(List.of(ProjectStatus.IN_PROGRESS))
            .pageable(pageable)
            .build();
    }

    /**
     * Admin용 — {@code statuses}를 자유 지정합니다.
     * null/빈 리스트면 {@code [IN_PROGRESS]}로 default 처리합니다.
     */
    public static SearchProjectQuery forAdmin(
        Long gisuId,
        String keyword,
        Long chapterId,
        List<Long> schoolIds,
        List<ChallengerPart> parts,
        PartQuotaStatus partQuotaStatus,
        List<ProjectStatus> statuses,
        Pageable pageable
    ) {
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
