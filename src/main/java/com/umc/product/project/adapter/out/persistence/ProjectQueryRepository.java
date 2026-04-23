package com.umc.product.project.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

/**
 * Project QueryDSL 동적 검색 구현 (PROJECT-001).
 */
@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Project> search(SearchProjectQuery query) {
        // TODO: QueryDSL로 동적 조건 구현
        //  - gisuId (필수), keyword (name LIKE), chapterId, schoolIds (IN),
        //    parts (IN, project_part_quota join), partQuotaStatus (집계 서브쿼리),
        //    statuses (IN)
        //  - 정렬: createdAt DESC 기본
        //  - count 쿼리 분리 필요
        throw new UnsupportedOperationException("TODO: ProjectQueryRepository.search 구현 필요");
    }
}
