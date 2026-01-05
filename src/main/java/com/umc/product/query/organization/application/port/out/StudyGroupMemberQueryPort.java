package com.umc.product.query.organization.application.port.out;

import com.umc.product.command.organization.domain.StudyGroup;
import com.umc.product.command.organization.domain.StudyGroupMember;
import java.util.List;
import java.util.Optional;

public interface StudyGroupMemberQueryPort {

    Optional<StudyGroupMember> findById(Long id);

    List<StudyGroupMember> findByStudyGroup(StudyGroup studyGroup);
}
