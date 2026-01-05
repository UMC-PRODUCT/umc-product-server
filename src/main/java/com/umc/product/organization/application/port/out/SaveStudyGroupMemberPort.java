package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.StudyGroupMember;

public interface SaveStudyGroupMemberPort {

    StudyGroupMember save(StudyGroupMember studyGroupMember);

    void delete(StudyGroupMember studyGroupMember);
}
