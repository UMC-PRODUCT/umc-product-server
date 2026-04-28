package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import java.util.List;
import java.util.Optional;

public interface LoadStudyGroupMemberPort {

    Optional<StudyGroupMember> findById(Long id);

    List<StudyGroupMember> findByStudyGroup(StudyGroup studyGroup);
}
