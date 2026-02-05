package com.umc.product.organization.application.port.service.command;

import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.in.command.ManageStudyGroupUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupCommand;
import com.umc.product.organization.application.port.out.command.ManageStudyGroupPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupCommandService implements ManageStudyGroupUseCase {

    private final LoadStudyGroupPort loadStudyGroupPort;
    private final LoadGisuPort loadGisuPort;
    private final ManageStudyGroupPort manageStudyGroupPort;
    private final LoadChallengerPort loadChallengerPort;

    @Override
    public void create(CreateStudyGroupCommand command) {
        validateChallengerIdsExist(command.leaderId(), command.memberIds());

        Gisu gisu = loadGisuPort.findActiveGisu();

        StudyGroup studyGroup = StudyGroup.create(command.name(), gisu, command.part());

        studyGroup.addMember(command.leaderId(), true);

        if (command.memberIds() != null) {
            command.memberIds().forEach(studyGroup::addMember);
        }

        manageStudyGroupPort.save(studyGroup);
    }

    @Override
    public void update(UpdateStudyGroupCommand command) {
        StudyGroup studyGroup = loadStudyGroupPort.findById(command.groupId());

        studyGroup.updateName(command.name());
        studyGroup.updatePart(command.part());

        manageStudyGroupPort.save(studyGroup);
    }

    @Override
    public void delete(Long groupId) {
        StudyGroup studyGroup = loadStudyGroupPort.findById(groupId);

        manageStudyGroupPort.delete(studyGroup);
    }

    private void validateChallengerIdsExist(Long leaderId, Set<Long> memberIds) {
        Set<Long> challengerIds = new HashSet<>();
        challengerIds.add(leaderId);
        if (memberIds != null) {
            challengerIds.addAll(memberIds);
        }

        Long count = loadChallengerPort.countByIdIn(challengerIds);

        if (count != challengerIds.size()) {
            throw new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.STUDY_GROUP_CHALLENGER_INVALID);
        }
    }
}
