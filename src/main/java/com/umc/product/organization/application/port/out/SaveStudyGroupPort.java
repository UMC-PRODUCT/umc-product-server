package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.StudyGroup;

public interface SaveStudyGroupPort {

    StudyGroup save(StudyGroup studyGroup);
    void delete(StudyGroup studyGroup);
}
