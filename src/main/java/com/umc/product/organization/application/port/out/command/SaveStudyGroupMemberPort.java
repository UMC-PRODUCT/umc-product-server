package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.StudyGroupMember;
import java.util.List;

public interface SaveStudyGroupMemberPort {

    StudyGroupMember save(StudyGroupMember studyGroupMember);

    List<StudyGroupMember> saveAll(List<StudyGroupMember> studyGroupMember);

    void delete(StudyGroupMember studyGroupMember);

    void deleteAll(List<StudyGroupMember> studyGroupMember);
}
