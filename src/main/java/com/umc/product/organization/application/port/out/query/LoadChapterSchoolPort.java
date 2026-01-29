package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;

public interface LoadChapterSchoolPort {

    //    Optional<ChapterSchool> findById(Long id);
    ChapterSchool findByChapterIdAndSchoolId(Long chapterId, Long schoolId);

    List<ChapterSchool> findBySchoolId(Long schoolId);
    List<ChapterSchool> findByGisuId(Long gisuId);
}
