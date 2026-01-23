package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.RecruitmentSchedule;

public interface SaveRecruitmentSchedulePort {
    RecruitmentSchedule save(RecruitmentSchedule schedule);

    void saveAll(Iterable<RecruitmentSchedule> schedules);

    void deleteAllByRecruitmentId(Long recruitmentId);
}
