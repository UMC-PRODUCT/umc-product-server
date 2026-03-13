package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
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

    @Override
    public List<SchoolStudyGroupInfo> findSchoolsWithStudyGroups() {
        return studyGroupQueryRepository.findSchoolsWithStudyGroups();
    }

    @Override
    public PartSummaryInfo findPartSummary(Long schoolId) {
        return studyGroupQueryRepository.findPartSummary(schoolId);
    }

    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> findStudyGroups(Long schoolId, ChallengerPart part, Long cursor,
                                                                   int size) {
        return studyGroupQueryRepository.findStudyGroups(schoolId, part, cursor, size);
    }

    @Override
    public List<StudyGroupNameInfo> findStudyGroupNames(Long schoolId, ChallengerPart part) {
        return studyGroupQueryRepository.findStudyGroupNames(schoolId, part);
    }

    @Override
    public StudyGroupDetailInfo findStudyGroupDetail(Long groupId) {
        return studyGroupQueryRepository.findStudyGroupDetail(groupId);
    }

    @Override
    public List<Long> findIdsByGisuIdAndPartIn(Long gisuId, Set<ChallengerPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return List.of();
        }
        return studyGroupJpaRepository.findIdsByGisuIdAndPartIn(gisuId, parts);
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
