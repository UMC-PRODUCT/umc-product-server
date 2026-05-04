package com.umc.product.curriculum.adapter.out.persistence.repository.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.umc.product.curriculum.domain.QOriginalWorkbook.originalWorkbook;

@Repository
@RequiredArgsConstructor
public class WeeklyCurriculumQueryRepository {

    private final JPAQueryFactory queryFactory;

    public boolean existsOriginalWorkbook(Long weeklyCurriculumId, OriginalWorkbookStatus status) {
        return queryFactory
            .selectOne()
            .from(originalWorkbook)
            .where(
                originalWorkbook.weeklyCurriculum.id.eq(weeklyCurriculumId),
                statusEq(status)
            )
            .fetchFirst() != null;
    }

    private BooleanExpression statusEq(OriginalWorkbookStatus status) {
        return status != null ? originalWorkbook.originalWorkbookStatus.eq(status) : null;
    }
}
