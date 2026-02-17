package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoadRecruitmentSchedulePort {

    List<RecruitmentSchedule> findByRecruitmentId(Long recruitmentId);

    RecruitmentSchedule findByRecruitmentIdAndType(Long recruitmentId, RecruitmentScheduleType type);

    Optional<RecruitmentSchedule> findOptionalByRecruitmentIdAndType(Long recruitmentId, RecruitmentScheduleType type);

    Map<RecruitmentScheduleType, RecruitmentSchedule> findScheduleMapByRecruitmentId(Long recruitmentId);

    Map<Long, RecruitmentSchedule> findScheduleMapByRecruitmentIdsAndType(List<Long> recruitmentIds,
                                                                          RecruitmentScheduleType type);
}
