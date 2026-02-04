package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import com.umc.product.organization.application.port.in.command.dto.AssignSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UnassignSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.application.port.out.query.LoadChapterSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.School;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolService implements ManageSchoolUseCase {

    private final LoadChapterPort loadChapterPort;
    private final LoadSchoolPort loadSchoolPort;
    private final ManageSchoolPort manageSchoolPort;
    private final ManageChapterSchoolPort manageChapterSchoolPort;
    private final LoadChapterSchoolPort loadChapterSchoolPort;

    @Override
    public Long register(CreateSchoolCommand command) {

        School newSchool = School.create(command.schoolName(), command.remark());
        newSchool.updateLogoImageId(command.logoImageId());
        School savedSchool = manageSchoolPort.save(newSchool);

        if (command.chapterId() != null) {
            Chapter chapter = loadChapterPort.findById(command.chapterId());

            ChapterSchool chapterSchool = ChapterSchool.create(chapter, savedSchool);
            manageChapterSchoolPort.save(chapterSchool);
        }

        return savedSchool.getId();
    }

    public void updateSchool(Long schoolId, UpdateSchoolCommand command) {

        School school = loadSchoolPort.findById(schoolId);

        school.updateName(command.schoolName());
        school.updateRemark(command.remark());
        school.updateLogoImageId(command.logoImageId());

        if (command.chapterId() != null) {
            Chapter chapter = loadChapterPort.findById(command.chapterId());

            school.updateChapterSchool(chapter);
        }
    }

    @Override
    public void deleteSchools(List<Long> schoolIds) {
        manageChapterSchoolPort.deleteAllBySchoolIds(schoolIds);
        manageSchoolPort.deleteAllByIds(schoolIds);
    }

    @Override
    public void assignToChapter(AssignSchoolCommand command) {
        School school = loadSchoolPort.findSchoolDetailById(command.schoolId());
        Chapter chapter = loadChapterPort.findById(command.chapterId());

        school.assignToChapter(chapter);
    }

    @Override
    public void unassignFromChapter(UnassignSchoolCommand command) {
        School school = loadSchoolPort.findSchoolDetailById(command.schoolId());

        school.unassignFromGisu(command.gisuId());
    }
}
