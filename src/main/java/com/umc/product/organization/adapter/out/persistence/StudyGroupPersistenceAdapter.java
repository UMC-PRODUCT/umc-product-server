package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupViewScope;
import com.umc.product.organization.application.port.out.command.ManageStudyGroupPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupPersistenceAdapter implements ManageStudyGroupPort, LoadStudyGroupPort {

    private final StudyGroupJpaRepository studyGroupJpaRepository;
    private final StudyGroupQueryRepository studyGroupQueryRepository;

    @Override
    public StudyGroup findById(Long id) {
        return studyGroupJpaRepository.findById(id).orElseThrow(
            () -> new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_NOT_FOUND));
    }

    @Override
    public StudyGroup findByName(String name) {
        return studyGroupJpaRepository.findByName(name).orElseThrow(
            () -> new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_NOT_FOUND));
    }

    /**
     * 역할 Scope 기반 "내 스터디 그룹" 조회를 위임한다.
     * <p>
     * scopes가 비어있으면 EXISTS 서브쿼리가 전부 false가 되어 풀 스캔을 유발할 수 있으므로,
     * Adapter 레벨에서 짧은 회로로 빈 리스트를 반환해 DB 호출 자체를 생략한다.
     */
    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> findMyStudyGroups(List<StudyGroupViewScope> scopes, Long gisuId,
                                                                     Long cursor, int size) {
        if (scopes == null || scopes.isEmpty()) {
            return List.of();
        }
        return studyGroupQueryRepository.findMyStudyGroups(scopes, gisuId, cursor, size);
    }

    @Override
    public List<StudyGroupNameInfo> findStudyGroupNames(List<StudyGroupViewScope> scopes, Long gisuId) {
        if (scopes == null || scopes.isEmpty()) {
            return List.of();
        }
        return studyGroupQueryRepository.findStudyGroupNames(scopes, gisuId);
    }


    @Override
    public List<StudyGroupMemberInfo> findStudyGroupMembers(Long groupId) {
        return studyGroupQueryRepository.findStudyGroupMembers(groupId);
    }

    @Override
    public List<Long> findIdsByGisuIdAndPartIn(Long gisuId, Set<ChallengerPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return List.of();
        }
        return studyGroupJpaRepository.findIdsByGisuIdAndPartIn(gisuId, parts);
    }

    @Override
    public Set<Long> findConflictedMemberIds(Long gisuId, ChallengerPart part, Set<Long> memberIds) {
        if(memberIds == null || memberIds.isEmpty()) {
            return Set.of();
        }
        return studyGroupQueryRepository.findConflictedMemberIds(gisuId, part, memberIds);

    }

    @Override
    public StudyGroup save(StudyGroup studyGroup) {
        return studyGroupJpaRepository.save(studyGroup);
    }

    @Override
    public void delete(StudyGroup studyGroup) {
        studyGroupJpaRepository.delete(studyGroup);
    }
}
