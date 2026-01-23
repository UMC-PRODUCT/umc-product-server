package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import java.util.List;

public interface LoadRecruitmentSchedulePort {

    List<RecruitmentSchedule> findByRecruitmentId(Long recruitmentId);

    RecruitmentSchedule findByRecruitmentIdAndType(Long recruitmentId, RecruitmentScheduleType type);

}
