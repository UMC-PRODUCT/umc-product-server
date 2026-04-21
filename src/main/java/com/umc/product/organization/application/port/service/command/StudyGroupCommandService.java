package com.umc.product.organization.application.port.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.command.ManageStudyGroupUseCase;
import com.umc.product.organization.application.port.in.command.dto.AddStudyGroupMembersCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupCommand;
import com.umc.product.organization.application.port.out.command.ManageStudyGroupPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
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

    @Override
    public void create(CreateStudyGroupCommand command) {

        // TODO: gisuInfo로 수정해야함
        Gisu gisu = loadGisuPort.findActiveGisu();

        validateNoPartStudyConflict(gisu.getId(), command.part(), command.memberIds());

        StudyGroup studyGroup = StudyGroup.create(command.name(), gisu.getId(), command.part(),
            command.organizerIds(), command.memberIds());

        manageStudyGroupPort.save(studyGroup);
    }

    @Override
    public void update(UpdateStudyGroupCommand command) {
        StudyGroup studyGroup = loadStudyGroupPort.findById(command.groupId());

        studyGroup.updateName(command.name());

        if(command.part() != null) {
            studyGroup.updatePart(ChallengerPart.from((command.part())));
        }

        manageStudyGroupPort.save(studyGroup);
    }

    @Override
    public void addMembers(AddStudyGroupMembersCommand command) {
        StudyGroup studyGroup = loadStudyGroupPort.findById(command.groupId());

        // 1. 이미 소속된 멤버 검증 (도메인 내부 규칙)
        studyGroup.validateMembersNotJoined(command.memberIds());

        // 2. 다른 파트 스터디와 충돌 검증 (여러 도메인 거친 검사)
        validateNoPartStudyConflict(studyGroup.getGisuId(), studyGroup.getPart(), command.memberIds());

        // 3. 추가 & 저장
        command.memberIds().forEach(studyGroup::addStudyGroupMember);
    }


    private void validateNoPartStudyConflict(Long gisuId, ChallengerPart part, Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }
        Set<Long> conflictMemberIds = loadStudyGroupPort.findConflictedMemberIds(gisuId, part, memberIds);
        if (!conflictMemberIds.isEmpty()) {
            throw new OrganizationDomainException(
                OrganizationErrorCode.STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY,
                "다른 스터디 그룹과 중복된 멤버가 있습니다. 충돌하는 멤버 ID: " + conflictMemberIds);
        }
    }

    @Override
    public void delete(Long groupId) {
        StudyGroup studyGroup = loadStudyGroupPort.findById(groupId);

        manageStudyGroupPort.delete(studyGroup);
    }

}
