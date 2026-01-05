package com.umc.product.command.organization.application.port.out;

import com.umc.product.command.organization.domain.StudyGroupMember;

public interface StudyGroupMemberCommandPort {

    StudyGroupMember save(StudyGroupMember studyGroupMember);

    void delete(StudyGroupMember studyGroupMember);
}
