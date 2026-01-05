package com.umc.product.query.organization.application.port.out;

import com.umc.product.command.organization.domain.Chapter;
import com.umc.product.command.organization.domain.ChapterSchool;
import com.umc.product.command.organization.domain.School;
import java.util.List;
import java.util.Optional;

public interface ChapterSchoolQueryPort {

    Optional<ChapterSchool> findById(Long id);

    Optional<ChapterSchool> findByChapterAndSchool(Chapter chapter, School school);

    List<ChapterSchool> findAllByChapter(Chapter chapter);
}
