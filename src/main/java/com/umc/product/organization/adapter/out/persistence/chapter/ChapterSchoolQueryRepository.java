package com.umc.product.organization.adapter.out.persistence.chapter;

import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.QChapter;
import com.umc.product.organization.domain.QGisu;
import com.umc.product.organization.domain.QSchool;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChapterSchoolQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Optional<ChapterSchool> findByChapterIdAndSchoolId(Long chapterId, Long schoolId) {
        return Optional.ofNullable(
            jpaQueryFactory
                .selectFrom(chapterSchool)
                .where(chapterSchool.chapter.id.eq(chapterId),
                    chapterSchool.school.id.eq(schoolId))
                .fetchOne()
        );
    }

    public List<ChapterSchool> findBySchoolId(Long schoolId) {
        return jpaQueryFactory
            .selectFrom(chapterSchool)
            .where(chapterSchool.school.id.eq(schoolId))
            .fetch();
    }

    public List<ChapterSchool> findBySchoolIdIn(Collection<Long> schoolIds) {
        return jpaQueryFactory
            .selectFrom(chapterSchool)
            .where(chapterSchool.school.id.in(schoolIds))
            .fetch();
    }

    /**
     * 여러 gisuId와 schoolId 조합에 해당하는 ChapterSchool을 1번 쿼리로 일괄 조회 chapter, chapter.gisu, school을 fetch join하여 lazy load
     * 방지
     */
    public List<ChapterSchool> findByGisuIdInAndSchoolIdIn(Set<Long> gisuIds, Set<Long> schoolIds) {
        QChapter chapter = QChapter.chapter;
        QGisu gisu = QGisu.gisu;
        QSchool school = QSchool.school;

        return jpaQueryFactory
            .selectFrom(chapterSchool)
            .join(chapterSchool.chapter, chapter).fetchJoin()
            .join(chapter.gisu, gisu).fetchJoin()
            .join(chapterSchool.school, school).fetchJoin()
            .where(
                gisu.id.in(gisuIds),
                school.id.in(schoolIds)
            )
            .fetch();
    }

    /**
     * 여러 gisuId에 속한 ChapterSchool을 1번 쿼리로 일괄 조회하고 chapter, chapter.gisu, school을 fetch join합니다.
     */
    public List<ChapterSchool> findByGisuIdIn(Set<Long> gisuIds) {
        QChapter chapter = QChapter.chapter;
        QGisu gisu = QGisu.gisu;
        QSchool school = QSchool.school;

        return jpaQueryFactory
            .selectFrom(chapterSchool)
            .join(chapterSchool.chapter, chapter).fetchJoin()
            .join(chapter.gisu, gisu).fetchJoin()
            .join(chapterSchool.school, school).fetchJoin()
            .where(gisu.id.in(gisuIds))
            .fetch();
    }
}
