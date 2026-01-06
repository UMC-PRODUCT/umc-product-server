package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.ChapterSchool;

public interface ManageChapterSchoolPort {

    ChapterSchool save(ChapterSchool chapterSchool);
    void delete(ChapterSchool chapterSchool);
}
