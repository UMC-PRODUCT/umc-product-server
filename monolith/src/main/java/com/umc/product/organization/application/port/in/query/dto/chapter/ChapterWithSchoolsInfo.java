package com.umc.product.organization.application.port.in.query.dto.chapter;

import java.util.List;

import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;

public record ChapterWithSchoolsInfo(
    Long chapterId,
    String chapterName,
    List<SchoolInfo> schools
) {

    public static ChapterWithSchoolsInfo from(Chapter chapter, List<ChapterSchool> chapterSchools) {
        List<SchoolInfo> schools = chapterSchools.stream()
            .map(cs -> new SchoolInfo(
                cs.getSchool().getId(),
                cs.getSchool().getName()
            ))
            .toList();
        return new ChapterWithSchoolsInfo(chapter.getId(), chapter.getName(), schools);
    }

    public record SchoolInfo(
        Long schoolId,
        String schoolName
    ) {
    }
}
