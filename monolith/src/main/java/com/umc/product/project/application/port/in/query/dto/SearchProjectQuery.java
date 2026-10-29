package com.umc.product.project.application.port.in.query.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.PartQuotaStatus;
import com.umc.product.project.domain.enums.ProjectStatus;

import lombok.Builder;

/**
 * 프로젝트 목록 검색 Query (PROJECT-001).
 * <p>
 * 권한(챌린저 vs Admin)에 따라 노출되는 상태 범위가 다릅니다.
 * 직접 {@code builder()}/생성자로 만들지 말고 {@link #forChallenger} 또는 {@link #forAdmin}을 사용하세요.
 * Controller는 분기 로직을 가지지 않고, Request DTO의 {@code toQuery(...)}에서 팩토리를 선택합니다.
 * <p>
 * Service 계층에서 {@link com.umc.product.project.application.access.ProjectAccessScope} 적용 시
 * {@link #withStatuses}, {@link #withChapterFilter}, {@link #withOwnerFilter}
 * 헬퍼로 새 인스턴스를 만들어 어댑터에 전달합니다.
 *
 * @param requestedChapterId 관리 목록에서 요청한 지부. 권한 범위의 chapterId와 별도로 AND 필터를 적용한다.
 * @param matchingListOrder 팀 매칭 목록의 제출순·모집 완료 하단 정렬 여부.
 * @param matchingOrderAt 서버에서 결정하는 팀 매칭 기간 판정 시각.
 * @param managedStatusOrder 관리 목록 전용 상태 우선 정렬 여부. 공개 목록은 false 로 기존 정렬을 유지한다.
 */
@Builder
public record SearchProjectQuery(
    Long gisuId,
    String keyword,
    Long chapterId,
    List<Long> productOwnerSchoolIds,
    Long productOwnerMemberId,
    Long includedOwnerMemberId,
    List<ChallengerPart> parts,
    PartQuotaStatus partQuotaStatus,
    List<ProjectStatus> statuses,
    List<ProjectStatus> includedOwnerStatuses,
    List<ProjectStatus> globalVisibleStatuses,
    Pageable pageable,
    boolean managedStatusOrder,
    boolean matchingListOrder,
    Instant matchingOrderAt,
    Long requestedChapterId
) {
    public SearchProjectQuery {
        if (globalVisibleStatuses != null && !globalVisibleStatuses.isEmpty() && chapterId == null) {
            throw new IllegalArgumentException("chapterId must not be null when globalVisibleStatuses exists");
        }
        Objects.requireNonNull(gisuId, "gisuId must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");
        if (statuses == null || statuses.isEmpty()) {
            throw new IllegalArgumentException("statuses must contain at least one ProjectStatus");
        }
        if (includedOwnerMemberId == null && includedOwnerStatuses != null && !includedOwnerStatuses.isEmpty()) {
            throw new IllegalArgumentException("includedOwnerMemberId must not be null when includedOwnerStatuses exists");
        }
        if (includedOwnerMemberId != null && (includedOwnerStatuses == null || includedOwnerStatuses.isEmpty())) {
            throw new IllegalArgumentException("includedOwnerStatuses must contain at least one ProjectStatus");
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
        List<Long> productOwnerSchoolIds,
        List<ChallengerPart> parts,
        PartQuotaStatus partQuotaStatus,
        Pageable pageable
    ) {
        return SearchProjectQuery.builder()
            .gisuId(gisuId)
            .keyword(keyword)
            .chapterId(chapterId)
            .productOwnerSchoolIds(productOwnerSchoolIds)
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
        List<Long> productOwnerSchoolIds,
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
            .productOwnerSchoolIds(productOwnerSchoolIds)
            .parts(parts)
            .partQuotaStatus(partQuotaStatus)
            .statuses(effectiveStatuses)
            .pageable(pageable)
            .build();
    }

    /** scope 적용 — 상태 필터만 교체. */
    public SearchProjectQuery withStatuses(Set<ProjectStatus> newStatuses) {
        return copyBuilder()
            .statuses(toList(newStatuses))
            .build();
    }

    /** scope 적용 — 지부 한정 + 상태 교체. */
    public SearchProjectQuery withChapterFilter(Long chapterId, Set<ProjectStatus> newStatuses) {
        return copyBuilder()
            .chapterId(chapterId)
            .statuses(toList(newStatuses))
            .build();
    }

    /** 지부장·중앙운영진 겸직의 상태별 조회 범위를 한 쿼리에 적용한다. */
    public SearchProjectQuery withChapterAndGlobalRead(Long scopedChapterId, Set<ProjectStatus> chapterStatuses,
        Set<ProjectStatus> globalStatuses) {
        return copyBuilder().chapterId(scopedChapterId).statuses(toList(chapterStatuses))
            .globalVisibleStatuses(toRequiredList(globalStatuses)).build();
    }

    /** scope 적용 — owner 한정 + 상태 교체. */
    public SearchProjectQuery withOwnerFilter(Long memberId, Set<ProjectStatus> newStatuses) {
        return copyBuilder()
            .productOwnerMemberId(memberId)
            .statuses(toList(newStatuses))
            .build();
    }

    /** scope 적용 — 기존 scope 조건에 본인 PO 프로젝트를 OR 로 추가 포함. */
    public SearchProjectQuery withIncludedOwner(Long memberId, Set<ProjectStatus> ownerStatuses) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        return copyBuilder()
            .includedOwnerMemberId(memberId)
            .includedOwnerStatuses(toRequiredList(ownerStatuses))
            .build();
    }

    /** 팀 매칭 목록의 기간 판정 시각은 서버에서 지정한다. */
    public SearchProjectQuery withMatchingOrderAt(Instant now) {
        return copyBuilder().matchingOrderAt(Objects.requireNonNull(now)).build();
    }

    private SearchProjectQueryBuilder copyBuilder() {
        return SearchProjectQuery.builder()
            .gisuId(gisuId)
            .keyword(keyword)
            .chapterId(chapterId)
            .productOwnerSchoolIds(productOwnerSchoolIds)
            .productOwnerMemberId(productOwnerMemberId)
            .includedOwnerMemberId(includedOwnerMemberId)
            .parts(parts)
            .partQuotaStatus(partQuotaStatus)
            .statuses(statuses)
            .includedOwnerStatuses(includedOwnerStatuses)
            .globalVisibleStatuses(globalVisibleStatuses)
            .pageable(pageable)
            .managedStatusOrder(managedStatusOrder)
            .matchingListOrder(matchingListOrder)
            .matchingOrderAt(matchingOrderAt)
            .requestedChapterId(requestedChapterId);
    }

    private static List<ProjectStatus> toList(Set<ProjectStatus> set) {
        if (set == null || set.isEmpty()) {
            return List.of(ProjectStatus.IN_PROGRESS);
        }
        return new ArrayList<>(set);
    }

    private static List<ProjectStatus> toRequiredList(Set<ProjectStatus> set) {
        if (set == null || set.isEmpty()) {
            throw new IllegalArgumentException("ownerStatuses must contain at least one ProjectStatus");
        }
        return new ArrayList<>(set);
    }
}
