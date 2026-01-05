package com.umc.product.command.organization.application.port.out;

import com.umc.product.command.organization.domain.ChapterSchool;

public interface ChapterSchoolCommandPort {

    ChapterSchool save(ChapterSchool chapterSchool);
    void delete(ChapterSchool chapterSchool);
}
