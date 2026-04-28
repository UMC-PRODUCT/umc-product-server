package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupMemberJpaRepository extends JpaRepository<StudyGroupMember, Long> {

    List<StudyGroupMember> findByStudyGroup(StudyGroup studyGroup);
}
