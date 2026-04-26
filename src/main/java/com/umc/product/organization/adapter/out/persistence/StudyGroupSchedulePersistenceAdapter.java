package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.application.port.out.command.SaveStudyGroupSchedulePort;
import com.umc.product.organization.domain.StudyGroupSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupSchedulePersistenceAdapter implements SaveStudyGroupSchedulePort {

    private final StudyGroupScheduleJpaRepository jpaRepository;

    @Override
    public StudyGroupSchedule save(StudyGroupSchedule studyGroupSchedule) {
        return jpaRepository.save(studyGroupSchedule);
    }
}
