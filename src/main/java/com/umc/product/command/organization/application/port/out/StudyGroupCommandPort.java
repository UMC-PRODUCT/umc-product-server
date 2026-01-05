package com.umc.product.command.organization.application.port.out;

import com.umc.product.command.organization.domain.StudyGroup;

public interface StudyGroupCommandPort {

    StudyGroup save(StudyGroup studyGroup);
    void delete(StudyGroup studyGroup);
}
