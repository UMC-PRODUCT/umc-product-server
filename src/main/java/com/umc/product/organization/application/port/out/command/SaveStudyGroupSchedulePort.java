package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.StudyGroupSchedule;

public interface SaveStudyGroupSchedulePort {

    StudyGroupSchedule save(StudyGroupSchedule studyGroupSchedule);
}
