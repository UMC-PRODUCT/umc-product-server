package com.umc.product.organization.adapter.out.persistence.studygroup;

import com.umc.product.organization.application.port.out.command.SaveStudyGroupMentorPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupMentorPort;
import com.umc.product.organization.domain.StudyGroupMentor;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupMentorPersistenceAdapter
    implements SaveStudyGroupMentorPort, LoadStudyGroupMentorPort {

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
    public Optional<StudyGroupMentor> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<StudyGroupMentor> findByStudyGroupId(Long studyGroupId) {
        return jpaRepository.findByStudyGroup_Id(studyGroupId);
    }
}
