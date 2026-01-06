package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.StudyGroup;

public interface ManageStudyGroupPort {

    StudyGroup save(StudyGroup studyGroup);
    void delete(StudyGroup studyGroup);
}
