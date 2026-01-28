package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadChapterSchoolPort;
import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChapterSchoolPersistenceAdapter implements LoadChapterSchoolPort, ManageChapterSchoolPort {

    private final ChapterSchoolJpaRepository chapterSchoolJpaRepository;

    @Override
    public List<ChapterSchool> findByGisuId(Long gisuId) {
        return chapterSchoolJpaRepository.findByGisuIdWithChapterAndSchool(gisuId);
    }

    @Override
    public ChapterSchool save(ChapterSchool chapterSchool) {
        return chapterSchoolJpaRepository.save(chapterSchool);
    }

    @Override
    public void deleteAllBySchoolIds(List<Long> schoolIds) {
        chapterSchoolJpaRepository.deleteAllBySchoolIdIn(schoolIds);
    }
}
