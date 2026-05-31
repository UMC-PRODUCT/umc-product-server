package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.StudyGroupSchedule;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyGroupScheduleJpaRepository extends JpaRepository<StudyGroupSchedule, Long> {

    @Query("SELECT s.scheduleId FROM StudyGroupSchedule s WHERE s.studyGroupId IN :studyGroupIds")
    List<Long> findScheduleIdsByStudyGroupIdIn(@Param("studyGroupIds") Collection<Long> studyGroupIds);
}
