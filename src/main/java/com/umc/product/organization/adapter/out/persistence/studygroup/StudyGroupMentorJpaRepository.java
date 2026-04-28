package com.umc.product.organization.adapter.out.persistence.studygroup;

import com.umc.product.organization.domain.StudyGroupMentor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupMentorJpaRepository extends JpaRepository<StudyGroupMentor, Long> {

    List<StudyGroupMentor> findByStudyGroup_Id(Long studyGroupId);
}