package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.ChapterSchool;

public interface SaveChapterSchoolPort {

    ChapterSchool save(ChapterSchool chapterSchool);
    void delete(ChapterSchool chapterSchool);
}
