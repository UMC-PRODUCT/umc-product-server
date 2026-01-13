package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.RecruitmentSchedule;
import java.util.List;

public interface LoadRecruitmentSchedulePort {

    List<RecruitmentSchedule> findByRecruitmentId(Long recruitmentId);

}
