package com.umc.product.curriculum.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallengerWorkbook.challengerWorkbook;
import static com.umc.product.curriculum.domain.QCurriculum.curriculum;
import static com.umc.product.curriculum.domain.QOriginalWorkbook.originalWorkbook;
import static com.umc.product.organization.domain.QGisu.gisu;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo.WorkbookProgressInfo;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CurriculumQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 챌린저의 커리큘럼 진행 상황 조회 (활성 기수 기준)
     */
    public Optional<CurriculumProgressInfo> findCurriculumProgress(Long challengerId, ChallengerPart part) {
        // 1. 활성 기수의 커리큘럼 조회
        Curriculum curriculumEntity = queryFactory
                .selectFrom(curriculum)
                .join(gisu).on(gisu.id.eq(curriculum.gisuId))
                .where(
                        gisu.isActive.eq(true),
                        curriculum.part.eq(part)
                )
                .fetchOne();

        if (curriculumEntity == null) {
            return Optional.empty();
        }

        // 2. OriginalWorkbook + ChallengerWorkbook 조인 조회
        List<Tuple> results = queryFactory
                .select(
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
                )
                .from(originalWorkbook)
                .leftJoin(challengerWorkbook)
                .on(
                        challengerWorkbook.originalWorkbookId.eq(originalWorkbook.id),
                        challengerWorkbook.challengerId.eq(challengerId)
                )
                .where(originalWorkbook.curriculum.id.eq(curriculumEntity.getId()))
                .orderBy(originalWorkbook.weekNo.asc())
                .fetch();

        // 3. 결과 변환
        LocalDate today = LocalDate.now();
        List<WorkbookProgressInfo> workbooks = results.stream()
                .map(tuple -> {
                    Integer weekNo = tuple.get(originalWorkbook.weekNo);
                    Long challengerWorkbookId = tuple.get(challengerWorkbook.id);
                    WorkbookStatus challengerWorkbookStatus = tuple.get(challengerWorkbook.status);
                    LocalDate startDate = tuple.get(originalWorkbook.startDate);
                    LocalDate endDate = tuple.get(originalWorkbook.endDate);
                    boolean isReleased = tuple.get(originalWorkbook.releasedAt) != null;

                    // 상태 결정
                    WorkbookStatus finalStatus = challengerWorkbookId == null ? null : challengerWorkbookStatus;

                    boolean isInDateRange = startDate != null && endDate != null
                            && !today.isBefore(startDate)
                            && !today.isAfter(endDate);
                    boolean isInProgress = isReleased && isInDateRange;

                    return new WorkbookProgressInfo(
                            challengerWorkbookId,
                            weekNo,
                            tuple.get(originalWorkbook.title),
                            tuple.get(originalWorkbook.description),
                            tuple.get(originalWorkbook.missionType),
                            finalStatus,
                            isInProgress
                    );
                })
                .toList();

        int completedCount = (int) workbooks.stream()
                .map(WorkbookProgressInfo::status)
                .filter(status -> status == WorkbookStatus.PASS || status == WorkbookStatus.FAIL)
                .count();
        int totalCount = workbooks.size();

        return Optional.of(new CurriculumProgressInfo(
                curriculumEntity.getId(),
                curriculumEntity.getTitle(),
                part.name(),
                completedCount,
                totalCount,
                workbooks
        ));
    }
}
