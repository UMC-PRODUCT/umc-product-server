package com.umc.product.query.application.port.out;

import com.umc.product.command.organization.domain.StudyGroup;
import com.umc.product.command.organization.domain.StudyGroupMember;
import java.util.List;
import java.util.Optional;

public interface LoadStudyGroupMemberPort {

    Optional<StudyGroupMember> findById(Long id);

    List<StudyGroupMember> findByStudyGroup(StudyGroup studyGroup);
}
