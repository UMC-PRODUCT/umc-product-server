package com.umc.product.curriculum.adapter.out.persistence;

import static com.umc.product.curriculum.domain.QChallengerWorkbook.challengerWorkbook;
import static com.umc.product.curriculum.domain.QCurriculum.curriculum;
import static com.umc.product.curriculum.domain.QOriginalWorkbook.originalWorkbook;
import static com.umc.product.organization.domain.QGisu.gisu;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumInfo.WorkbookInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProjection;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumWeekInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookProgressProjection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CurriculumQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<WorkbookProgressProjection> findWorkbookProgressProjections(Long curriculumId, Long challengerId) {
        return queryFactory
            .select(Projections.constructor(WorkbookProgressProjection.class,
                originalWorkbook.id,
                originalWorkbook.weekNo,
                originalWorkbook.title,
                originalWorkbook.description,
                originalWorkbook.missionType,
                originalWorkbook.startDate,
                originalWorkbook.endDate,
                originalWorkbook.releasedAt,
                challengerWorkbook.id,
                challengerWorkbook.status
            ))
            .from(originalWorkbook)
            .leftJoin(challengerWorkbook)
            .on(
                challengerWorkbook.originalWorkbookId.eq(originalWorkbook.id),
                challengerWorkbook.challengerId.eq(challengerId)
            )
            .where(originalWorkbook.curriculum.id.eq(curriculumId))
            .orderBy(originalWorkbook.weekNo.asc())
            .fetch();
    }

    /**
     * 활성 기수의 파트별 커리큘럼 주차 정보 조회 (weekNo, title만)
     */
    public List<CurriculumWeekInfo> findWeekInfoByActiveGisuAndPart(ChallengerPart part) {
        return queryFactory
            .select(Projections.constructor(
                CurriculumWeekInfo.class,
                originalWorkbook.weekNo,
                originalWorkbook.title
            ))
            .from(originalWorkbook)
            .join(curriculum).on(curriculum.id.eq(originalWorkbook.curriculum.id))
            .join(gisu).on(gisu.id.eq(curriculum.gisuId))
            .where(
                gisu.isActive.eq(true),
                curriculum.part.eq(part)
            )
            .orderBy(originalWorkbook.weekNo.asc())
            .fetch();
    }

    /**
     * 활성 기수에서 배포된(releasedAt IS NOT NULL) 주차 번호 목록 조회
     *
     * @param part 파트 (null이면 모든 파트의 주차를 합산)
     * @return 배포된 주차 번호 목록 (오름차순, 중복 제거)
     */
    public List<Integer> findReleasedWeekNos(ChallengerPart part) {
        return queryFactory
            .select(originalWorkbook.weekNo)
            .distinct()
            .from(originalWorkbook)
            .join(curriculum).on(curriculum.id.eq(originalWorkbook.curriculum.id))
            .join(gisu).on(gisu.id.eq(curriculum.gisuId))
            .where(
                gisu.isActive.eq(true),
                originalWorkbook.releasedAt.isNotNull(),
                partCondition(part)
            )
            .orderBy(originalWorkbook.weekNo.asc())
            .fetch();
    }

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

    public List<WorkbookInfo> fetchWorkbooks(Long curriculumId, Integer weekNo) {
        return queryFactory
            .select(Projections.constructor(WorkbookInfo.class,
                originalWorkbook.id,
                originalWorkbook.weekNo,
                originalWorkbook.title,
                originalWorkbook.description,
                originalWorkbook.workbookUrl,
                originalWorkbook.startDate,
                originalWorkbook.endDate,
                originalWorkbook.missionType,
                originalWorkbook.releasedAt,
                originalWorkbook.releasedAt.isNotNull()
            ))
            .from(originalWorkbook)
            .where(
                originalWorkbook.curriculum.id.eq(curriculumId),
                weekNoCondition(weekNo)
            )
            .orderBy(originalWorkbook.weekNo.asc())
            .fetch();
    }

    private BooleanExpression weekNoCondition(Integer weekNo) {
        return weekNo != null ? originalWorkbook.weekNo.eq(weekNo) : null;
    }

    private BooleanExpression partCondition(ChallengerPart part) {
        return part != null ? curriculum.part.eq(part) : null;
    }
}
