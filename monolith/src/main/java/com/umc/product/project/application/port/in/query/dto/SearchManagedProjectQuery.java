package com.umc.product.project.application.port.in.query.dto;

import java.util.Objects;

import org.springframework.data.domain.Pageable;

import lombok.Builder;

/**
 * 관리 화면용 프로젝트 검색 Query (PROJECT-006).
 * <p>
 * 최고관리자는 모든 기수, 총괄단은 해당 기수 전체 지부, 지부장은 본인 지부를 조회한다.
 * 일반 중앙운영·교육국은 해당 기수 전체 지부의 승인대기·등록완료만, PM은 본인 프로젝트를 조회한다.
 * 학교 운영진 역할만으로는 관리 목록을 제공하지 않는다.
 * <p>
 * 타인 초안은 제외하며, 본인이 PO인 프로젝트는 DRAFT도 포함한다.
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
