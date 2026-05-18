package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.application.port.out.command.SaveStudyGroupSchedulePort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupSchedulePort;
import com.umc.product.organization.domain.StudyGroupSchedule;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupSchedulePersistenceAdapter
    implements SaveStudyGroupSchedulePort, LoadStudyGroupSchedulePort {

    private final StudyGroupScheduleJpaRepository jpaRepository;

    @Override
    public StudyGroupSchedule save(StudyGroupSchedule studyGroupSchedule) {
        return jpaRepository.save(studyGroupSchedule);
    }

    @Override
    public Set<Long> findScheduleIdsByStudyGroupIds(Collection<Long> studyGroupIds) {
        if (studyGroupIds == null || studyGroupIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(jpaRepository.findScheduleIdsByStudyGroupIdIn(studyGroupIds));
    }
}
