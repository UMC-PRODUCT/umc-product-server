package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;

public interface ManageChapterSchoolPort {

    ChapterSchool save(ChapterSchool chapterSchool);

    void deleteAllBySchoolIds(List<Long> schoolIds);

    void deleteAllByChapterId(Long chapterId);
}
