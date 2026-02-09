package com.umc.product.organization.application.port.service.command;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.application.port.out.query.LoadChapterSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChapterService implements ManageChapterUseCase {

    private final LoadGisuPort loadGisuPort;
    private final LoadChapterPort loadChapterPort;
    private final LoadSchoolPort loadSchoolPort;
    private final LoadChapterSchoolPort loadChapterSchoolPort;
    private final ManageChapterPort manageChapterPort;
    private final ManageChapterSchoolPort manageChapterSchoolPort;

    @Override
    public Long create(CreateChapterCommand command) {
        Gisu gisu = loadGisuPort.findById(command.gisuId());
        validateChapterNameNotDuplicated(command.gisuId(), command.name());

        Chapter chapter = Chapter.builder()
                .gisu(gisu)
                .name(command.name())
                .build();
        Chapter savedChapter = manageChapterPort.save(chapter);

        if (!command.schoolIds().isEmpty()) {
            List<School> schools = loadSchoolPort.findAllByIds(command.schoolIds());
            validateAllSchoolsExist(command.schoolIds(), schools);
            validateSchoolsNotAssignedInGisu(command.schoolIds(), command.gisuId());

            for (School school : schools) {
                ChapterSchool chapterSchool = ChapterSchool.create(savedChapter, school);
                manageChapterSchoolPort.save(chapterSchool);
            }
        }

        return savedChapter.getId();
    }

    private void validateAllSchoolsExist(List<Long> requestedIds, List<School> foundSchools) {
        Set<Long> requestedSet = new HashSet<>(requestedIds);
        Set<Long> foundSet = foundSchools.stream()
                .map(School::getId)
                .collect(Collectors.toSet());

        if (!foundSet.containsAll(requestedSet)) {
            throw new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.SCHOOL_NOT_FOUND);
        }
    }

    private void validateChapterNameNotDuplicated(Long gisuId, String name) {
        boolean duplicated = loadChapterPort.findByGisuId(gisuId).stream()
                .anyMatch(chapter -> chapter.getName().equals(name));

        if (duplicated) {
            throw new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.CHAPTER_NAME_DUPLICATED);
        }
    }

    private void validateSchoolsNotAssignedInGisu(List<Long> schoolIds, Long gisuId) {
        Set<Long> requestedSet = new HashSet<>(schoolIds);

        Set<Long> alreadyAssignedSchoolIds = loadChapterSchoolPort.findByGisuId(gisuId).stream()
                .map(cs -> cs.getSchool().getId())
                .collect(Collectors.toSet());

        requestedSet.retainAll(alreadyAssignedSchoolIds);

        if (!requestedSet.isEmpty()) {
            throw new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.SCHOOL_ALREADY_ASSIGNED_TO_CHAPTER);
        }
    }
}
