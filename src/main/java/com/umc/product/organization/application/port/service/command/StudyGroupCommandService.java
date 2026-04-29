package com.umc.product.organization.application.port.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.command.ManageStudyGroupUseCase;
import com.umc.product.organization.application.port.in.command.dto.AddStudyMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.AddStudyMentorCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.DeleteStudyMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.DeleteStudyMentorCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupCommand;
import com.umc.product.organization.application.port.out.command.SaveStudyGroupMemberPort;
import com.umc.product.organization.application.port.out.command.SaveStudyGroupMentorPort;
import com.umc.product.organization.application.port.out.command.SaveStudyGroupPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupMemberPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupMentorPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import com.umc.product.organization.domain.StudyGroupMentor;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupCommandService implements ManageStudyGroupUseCase {

    private final LoadStudyGroupPort loadStudyGroupPort;
    private final LoadStudyGroupMemberPort loadStudyGroupMemberPort;
    private final LoadStudyGroupMentorPort loadStudyGroupMentorPort;

    private final SaveStudyGroupMemberPort saveStudyGroupMemberPort;
    private final SaveStudyGroupMentorPort saveStudyGroupMentorPort;

    private final LoadGisuPort loadGisuPort;
    private final SaveStudyGroupPort saveStudyGroupPort;

    @Override
    public void create(CreateStudyGroupCommand command) {
        // 생성하고자 하는 기수에 스터디를 생성
        Gisu gisu = loadGisuPort.findById(command.gisuId());
        validateNoPartStudyConflict(gisu.getId(), command.part(), command.memberIds());

        // 스터디 그룹 먼저 생성
        StudyGroup studyGroup = saveStudyGroupPort.save(
            StudyGroup.create(command.name(), gisu.getId(), command.part()));

        // 스터디원 일괄 저장
        saveStudyGroupMemberPort.saveAll(
            command.memberIds().stream()
                .map(memberId -> StudyGroupMember.create(studyGroup, memberId))
                .toList()
        );

        // 파트장 일괄 저장
        saveStudyGroupMentorPort.saveAll(
            command.mentorIds().stream()
                .map(mentorId -> StudyGroupMentor.create(studyGroup, mentorId))
                .toList()
        );
    }

    @Override
    public void update(UpdateStudyGroupCommand command) {
        StudyGroup studyGroup = loadStudyGroupPort.getById(command.groupId());

        studyGroup.updateName(command.name());
        studyGroup.updatePart(command.part());

        saveStudyGroupPort.save(studyGroup);
    }

    @Override
    public void addMember(AddStudyMemberCommand command) {
        StudyGroup studyGroup = loadStudyGroupPort.getById(command.groupId());

        loadStudyGroupMemberPort.throwIfMemberAlreadyInStudyGroup(command.groupId(), command.memberId());
        saveStudyGroupMemberPort.save(
            StudyGroupMember.create(studyGroup, command.memberId())
        );
    }

    @Override
    public void addMentor(AddStudyMentorCommand command) {
        StudyGroup studyGroup = loadStudyGroupPort.getById(command.groupId());

        loadStudyGroupMentorPort.throwIfMentorAlreadyInStudyGroup(command.groupId(), command.mentorId());
        saveStudyGroupMentorPort.save(
            StudyGroupMentor.create(studyGroup, command.mentorId())
        );
    }

    @Override
    public void deleteMember(DeleteStudyMemberCommand command) {
        List<StudyGroupMember> currentMembers = loadStudyGroupMemberPort.listByStudyGroupId(command.groupId());

        // 1. 삭제할 멤버가 존재하는지 검증합니다.
        StudyGroupMember targetMember = currentMembers.stream()
            .filter(m -> m.getMemberId().equals(command.memberId()))
            .findFirst()
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_NOT_FOUND));

        // 2. 최소 1명은 존재해야 합니다. (스터디 그룹 삭제는 가능)
        if (currentMembers.size() == 1) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_REQUIRED);
        }

        // 3. 삭제
        saveStudyGroupMemberPort.delete(targetMember);
    }

    @Override
    public void deleteMentor(DeleteStudyMentorCommand command) {
        List<StudyGroupMentor> currentMentors = loadStudyGroupMentorPort.listByStudyGroupId(command.groupId());

        // 1. 삭제할 멤버가 존재하는지 검증합니다.
        StudyGroupMentor targetMentor = currentMentors.stream()
            .filter(m -> m.getMemberId().equals(command.mentorId()))
            .findFirst()
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MENTOR_NOT_FOUND));

        // 2. 최소 1명은 존재해야 합니다. (스터디 그룹 삭제는 가능)
        if (currentMentors.size() == 1) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MENTOR_REQUIRED);
        }

        // 3. 삭제
        saveStudyGroupMentorPort.delete(targetMentor);
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
    public void delete(Long studyGroupId) {
        StudyGroup studyGroup = loadStudyGroupPort.getById(studyGroupId);

        // orphan removal
        saveStudyGroupMemberPort.deleteAll(loadStudyGroupMemberPort.listByStudyGroupId(studyGroupId));
        saveStudyGroupMentorPort.deleteAll(loadStudyGroupMentorPort.listByStudyGroupId(studyGroupId));

        saveStudyGroupPort.delete(studyGroup);
    }

}
