package com.umc.product.organization.adapter.out.persistence;

import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
