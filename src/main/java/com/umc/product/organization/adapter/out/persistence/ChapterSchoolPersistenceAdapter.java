package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadChapterSchoolPort;
import com.umc.product.organization.domain.ChapterSchool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChapterSchoolPersistenceAdapter implements LoadChapterSchoolPort, ManageChapterSchoolPort {

    private final ChapterSchoolJpaRepository chapterSchoolJpaRepository;

    public void save(ChapterSchool chapterSchool) {
        chapterSchoolJpaRepository.save(chapterSchool);
    }

}
