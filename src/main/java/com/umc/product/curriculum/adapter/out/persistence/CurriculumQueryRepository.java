package com.umc.product.curriculum.adapter.out.persistence;

import static com.umc.product.curriculum.domain.QCurriculum.curriculum;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProjection;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.global.exception.NotImplementedException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CurriculumQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<CurriculumProjection> findByGisuIdAndPart(Long gisuId, ChallengerPart part) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(CurriculumProjection.class,
                    curriculum.id,
                    curriculum.part,
                    curriculum.title
                ))
                .from(curriculum)
                .where(curriculum.gisuId.eq(gisuId), curriculum.part.eq(part))
                .fetchOne()
        );
    }

    /**
     * 미배포 상태이면서 시작일이 지난 워크북 목록 조회 (자동 배포 대상)
     */
    public List<OriginalWorkbook> findUnreleasedWorkbookIdsWithStartDateBefore(Instant now) {
        throw new NotImplementedException();
        //        return queryFactory
//            .selectFrom(originalWorkbook)
//            .join(originalWorkbook.curriculum, curriculum).fetchJoin()
//            .where(
//                originalWorkbook.releasedAt.isNull(),
//                originalWorkbook.startDate.before(now)
//            )
//            .fetch();
    }
}