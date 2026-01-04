package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.RecruitmentSchedule;
import java.util.List;

public interface LoadRecruitmentSchedulePort {

    List<RecruitmentSchedule> findByRecruitmentId(Long recruitmentId);

}
