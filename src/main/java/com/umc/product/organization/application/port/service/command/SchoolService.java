package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.School;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolService implements ManageSchoolUseCase {

    private final LoadChapterPort loadChapterPort;
    private final ManageSchoolPort manageSchoolPort;
    private final ManageChapterSchoolPort manageChapterSchoolPort;

    public void register(CreateSchoolCommand command) {

        School school = School.create(command.name(), command.remark());
        School savedSchool = manageSchoolPort.save(school);

        if (command.chapterId() != null) {
            Chapter chapter = loadChapterPort.findById(command.chapterId());

            ChapterSchool chapterSchool = ChapterSchool.create(chapter, savedSchool);
            manageChapterSchoolPort.save(chapterSchool);
        }

    }

    public void updateSchool(UpdateSchoolCommand command) {

    }

    public void deleteSchool(Long schoolId) {

    }

    public void deleteSchools(java.util.List<Long> schoolIds) {

    }
}
