package com.umc.product.organization.adapter.out.persistence.studygroup;

import com.umc.product.organization.application.port.out.command.SaveStudyGroupMentorPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupMentorPort;
import com.umc.product.organization.domain.StudyGroupMentor;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupMentorPersistenceAdapter implements SaveStudyGroupMentorPort, LoadStudyGroupMentorPort {

    private final StudyGroupMentorJpaRepository jpaRepository;

    @Override
    public StudyGroupMentor save(StudyGroupMentor studyGroupMentor) {
        return jpaRepository.save(studyGroupMentor);
    }

    @Override
    public List<StudyGroupMentor> saveAll(List<StudyGroupMentor> studyGroupMentors) {
        return jpaRepository.saveAll(studyGroupMentors);
    }

    @Override
    public void delete(StudyGroupMentor studyGroupMentor) {
        jpaRepository.delete(studyGroupMentor);
    }

    @Override
    public void deleteAll(List<StudyGroupMentor> studyGroupMentors) {
        jpaRepository.deleteAll(studyGroupMentors);
    }

    @Override
    public Optional<StudyGroupMentor> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<StudyGroupMentor> listByStudyGroupId(Long studyGroupId) {
        return jpaRepository.findByStudyGroup_Id(studyGroupId);
    }

    @Override
    public void throwIfMentorAlreadyInStudyGroup(Long studyGroupId, Long mentorId) {
        if (jpaRepository.existsByStudyGroup_IdAndMemberId(studyGroupId, mentorId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MENTOR_DUPLICATED);
        }
    }
}
