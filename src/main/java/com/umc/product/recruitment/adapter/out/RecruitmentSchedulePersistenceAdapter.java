package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.RECRUITMENT_SCHEDULE_NOT_FOUND));
    }

    @Override
    public void deleteAllByRecruitmentId(Long recruitmentId) {
        recruitmentScheduleRepository.deleteAllByRecruitmentId(recruitmentId);
    }

    @Override
    public Optional<RecruitmentSchedule> findOptionalByRecruitmentIdAndType(Long recruitmentId,
                                                                            RecruitmentScheduleType type) {
        return recruitmentScheduleRepository.findByRecruitmentIdAndType(recruitmentId, type);
    }

    @Override
    public Map<RecruitmentScheduleType, RecruitmentSchedule> findScheduleMapByRecruitmentId(Long recruitmentId) {
        return recruitmentScheduleRepository.findByRecruitmentId(recruitmentId).stream()
            .collect(Collectors.toMap(
                RecruitmentSchedule::getType, // Key: 일정 타입
                schedule -> schedule          // Value: 일정 엔티티 객체
            ));
    }
}
