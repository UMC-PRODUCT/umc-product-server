package com.umc.product.organization.application.port.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.command.ManageStudyGroupUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceStudyGroupMemberAndMentorCommand;
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
        Gisu gisu = loadGisuPort.findById(command.gisuId());

        validateNoPartStudyConflict(gisu.getId(), command.part(), command.memberIds());

        StudyGroup studyGroup = StudyGroup.create(command.name(), gisu.getId(), command.part(),
            command.mentorIds(), command.memberIds());

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
    public void replaceMemberAndMentors(ReplaceStudyGroupMemberAndMentorCommand command) {
        StudyGroup studyGroup = loadStudyGroupPort.findById(command.groupId());

        // 1. 이미 소속된 멤버 검증 (도메인 내부 규칙)
        studyGroup.validateMembersNotJoined(command.studyMemberIds());

        // 2. 다른 파트 스터디와 충돌 검증 (여러 도메인 거친 검사)
        validateNoPartStudyConflict(studyGroup.getGisuId(), studyGroup.getPart(), command.studyMemberIds());

        // 3. 추가 & 저장
        command.studyMemberIds().forEach(studyGroup::addStudyGroupMember);
        manageStudyGroupPort.save(studyGroup);
    }


    private void validateNoPartStudyConflict(Long gisuId, ChallengerPart part, Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }

        // memberId의 목록에 들어있는 회원이 해당 기수에, 동일한 파트의 스터디에 참여하고 있는지를 검사
        Set<Long> conflictMemberIds = loadStudyGroupPort.findConflictedMemberIds(gisuId, part, memberIds);
        if (!conflictMemberIds.isEmpty()) {
            throw new OrganizationDomainException(
                OrganizationErrorCode.STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY,
                "제공된 회원 중에서 동일한 기수에 동일한 파트의 스터디에 참여하고 있는 회원이 있습니다. ID LIST: " + conflictMemberIds);
        }
    }

    @Override
    public void delete(Long groupId) {
        StudyGroup studyGroup = loadStudyGroupPort.findById(groupId);

        manageStudyGroupPort.delete(studyGroup);
    }

}
