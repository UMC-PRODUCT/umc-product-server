package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;
import java.util.Set;

public interface LoadChapterSchoolPort {

    //    Optional<ChapterSchool> findById(Long id);
    ChapterSchool findByChapterIdAndSchoolId(Long chapterId, Long schoolId);

    List<ChapterSchool> findBySchoolId(Long schoolId);
    List<ChapterSchool> findByGisuId(Long gisuId);

    /**
     * 여러 gisuId와 schoolId 조합에 해당하는 ChapterSchool 일괄 조회
     */
    List<ChapterSchool> findByGisuIdsAndSchoolIds(Set<Long> gisuIds, Set<Long> schoolIds);
}
