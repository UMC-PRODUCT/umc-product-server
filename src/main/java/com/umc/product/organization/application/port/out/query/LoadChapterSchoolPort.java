package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.School;
import java.util.List;
import java.util.Optional;

public interface LoadChapterSchoolPort {

    Optional<ChapterSchool> findById(Long id);

    Optional<ChapterSchool> findByChapterAndSchool(Chapter chapter, School school);

    List<ChapterSchool> findAllByChapter(Chapter chapter);
}
