package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.StudyGroupMentor;
import java.util.List;
import java.util.Optional;

public interface LoadStudyGroupMentorPort {

    Optional<StudyGroupMentor> findById(Long id);

    List<StudyGroupMentor> listByStudyGroupId(Long studyGroupId);

    void throwIfMentorAlreadyInStudyGroup(Long studyGroupId, Long mentorId);
}
