package com.umc.product.organization.application.port.out.query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.umc.product.organization.domain.ChapterSchool;

public interface LoadChapterSchoolPort {

    //    Optional<ChapterSchool> findById(Long id);
    ChapterSchool findByChapterIdAndSchoolId(Long chapterId, Long schoolId);

    List<ChapterSchool> findBySchoolId(Long schoolId);

    List<ChapterSchool> findBySchoolIds(Collection<Long> schoolIds);
    List<ChapterSchool> findByGisuId(Long gisuId);

    List<ChapterSchool> findByGisuIds(Set<Long> gisuIds);

    /**
     * 여러 gisuId와 schoolId 조합에 해당하는 ChapterSchool 일괄 조회
     */
    List<ChapterSchool> findByGisuIdsAndSchoolIds(Set<Long> gisuIds, Set<Long> schoolIds);
}
