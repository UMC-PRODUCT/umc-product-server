package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.StudyGroupMember;

public interface StudyGroupMemberManagePort {

    StudyGroupMember save(StudyGroupMember studyGroupMember);

    void delete(StudyGroupMember studyGroupMember);
}
