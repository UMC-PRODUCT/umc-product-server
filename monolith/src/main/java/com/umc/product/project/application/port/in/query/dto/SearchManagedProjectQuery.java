package com.umc.product.project.application.port.in.query.dto;

import java.util.Objects;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

/**
 * 관리 화면용 프로젝트 검색 Query (PROJECT-006).
 * <p>
 * 호출자 역할에 따라 자동 scope 적용 — 중앙 총괄단은 전체, 지부장은 본인 지부, 학교 회장단은 본인 학교,
 * PM 챌린저는 본인 owner 프로젝트만 노출. 일반 챌린저는 빈 결과.
 * <p>
 * 노출 상태: PENDING_REVIEW / IN_PROGRESS / COMPLETED / ABORTED (DRAFT 는 등록 화면에서 별도 조회).
 */
@Builder
public record SearchManagedProjectQuery(
    Long gisuId,
    String keyword,
    Pageable pageable
) {
    public SearchManagedProjectQuery {
        Objects.requireNonNull(gisuId, "gisuId must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");
    }
}
