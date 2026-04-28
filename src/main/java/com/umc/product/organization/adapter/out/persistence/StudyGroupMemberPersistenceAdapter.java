package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.application.port.out.command.ManageStudyGroupMemberPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupMemberPort;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupMemberPersistenceAdapter implements ManageStudyGroupMemberPort, LoadStudyGroupMemberPort {

    private final StudyGroupMemberJpaRepository jpaRepository;

    @Override
    public StudyGroupMember save(StudyGroupMember studyGroupMember) {
        return jpaRepository.save(studyGroupMember);
    }

    @Override
    public List<StudyGroupMember> saveAll(List<StudyGroupMember> studyGroupMember) {
        return jpaRepository.saveAll(studyGroupMember);
    }

    @Override
    public void delete(StudyGroupMember studyGroupMember) {
        jpaRepository.delete(studyGroupMember);
    }

    @Override
    public Optional<StudyGroupMember> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<StudyGroupMember> findByStudyGroup(StudyGroup studyGroup) {
        return jpaRepository.findByStudyGroup(studyGroup);
    }
}
