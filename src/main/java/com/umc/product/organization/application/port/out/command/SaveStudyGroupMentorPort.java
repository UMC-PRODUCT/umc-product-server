package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.StudyGroupMentor;
import java.util.List;

public interface SaveStudyGroupMentorPort {

    StudyGroupMentor save(StudyGroupMentor studyGroupMentor);

    List<StudyGroupMentor> saveAll(List<StudyGroupMentor> studyGroupMentors);

    void delete(StudyGroupMentor studyGroupMentor);

    void deleteAll(List<StudyGroupMentor> studyGroupMentors);
}
