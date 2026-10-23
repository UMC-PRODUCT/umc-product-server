package com.umc.product.organization.application.port.out.command;


import java.util.List;

import com.umc.product.organization.domain.ChapterSchool;

public interface SaveChapterSchoolPort {

    ChapterSchool save(ChapterSchool chapterSchool);

    void deleteAllBySchoolIds(List<Long> schoolIds);

    void deleteAllByChapterId(Long chapterId);
}
