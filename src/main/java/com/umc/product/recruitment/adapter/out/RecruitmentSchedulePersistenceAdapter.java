package com.umc.product.recruitment.adapter.out;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitmentSchedulePersistenceAdapter implements SaveRecruitmentSchedulePort, LoadRecruitmentSchedulePort {

    private final RecruitmentScheduleRepository recruitmentScheduleRepository;

    @Override
    public RecruitmentSchedule save(RecruitmentSchedule schedule) {
        return recruitmentScheduleRepository.save(schedule);
    }

    @Override
    public void saveAll(Iterable<RecruitmentSchedule> schedules) {
        recruitmentScheduleRepository.saveAll(schedules);
    }

    @Override
    public List<RecruitmentSchedule> findByRecruitmentId(Long recruitmentId) {
        return recruitmentScheduleRepository.findByRecruitmentId(recruitmentId);
    }

    @Override
    public RecruitmentSchedule findByRecruitmentIdAndType(Long recruitmentId, RecruitmentScheduleType type) {
        return recruitmentScheduleRepository.findByRecruitmentIdAndType(recruitmentId, type)
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_SCHEDULE_NOT_FOUND));
    }

    @Override
    public void deleteAllByRecruitmentId(Long recruitmentId) {
        recruitmentScheduleRepository.deleteAllByRecruitmentId(recruitmentId);
    }
}
