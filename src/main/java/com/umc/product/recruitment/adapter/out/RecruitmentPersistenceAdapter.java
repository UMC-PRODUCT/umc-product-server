package com.umc.product.recruitment.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.adapter.out.util.InterviewTimeTableDisabledCalculator;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo.ScheduleInfo;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPort;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitmentPersistenceAdapter implements SaveRecruitmentPort, LoadRecruitmentPort {
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentPartRepository recruitmentPartRepository;
    private final RecruitmentScheduleRepository recruitmentScheduleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Recruitment save(Recruitment recruitment) {
        return recruitmentRepository.save(recruitment);
    }

    @Override
    public void deleteById(Long recruitmentId) {
        recruitmentRepository.deleteById(recruitmentId);
    }

    @Override
    public Recruitment findById(Long recruitmentId) {
        return recruitmentRepository.findById(recruitmentId)
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
    }

    @Override
    public RecruitmentDraftInfo findDraftInfoById(Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow();

        List<ChallengerPart> parts =
                recruitmentPartRepository.findByRecruitmentId(recruitmentId)
                        .stream()
                        .map(RecruitmentPart::getPart)
                        .toList();

        List<RecruitmentSchedule> schedules =
                recruitmentScheduleRepository.findByRecruitmentId(recruitmentId);

        ScheduleInfo scheduleInfo = buildScheduleInfo(
                schedules,
                recruitment.getInterviewTimeTable()
        );

        return RecruitmentDraftInfo.from(
                recruitment,
                parts,
                scheduleInfo
        );
    }

    private RecruitmentDraftInfo.ScheduleInfo buildScheduleInfo(
            List<RecruitmentSchedule> schedules,
            Map<String, Object> interviewTimeTable
    ) {

        Instant applyStart = null, applyEnd = null;
        Instant docResult = null;
        Instant interviewStart = null, interviewEnd = null;
        Instant finalResult = null;

        for (RecruitmentSchedule s : schedules) {
            if (s.getType() == null) {
                continue;
            }

            switch (s.getType()) {
                case APPLY_WINDOW -> {
                    applyStart = s.getStartsAt();
                    applyEnd = s.getEndsAt();
                }
                case DOC_RESULT_AT -> docResult = s.getStartsAt(); // AT면 startsAt만 쓰는 규칙
                case INTERVIEW_WINDOW -> {
                    interviewStart = s.getStartsAt();
                    interviewEnd = s.getEndsAt();
                }
                case FINAL_RESULT_AT -> finalResult = s.getStartsAt();
                default -> {
                }
            }
        }

        RecruitmentDraftInfo.InterviewTimeTableInfo interviewTimeTableInfo =
                parseInterviewTimeTable(interviewTimeTable);

        return new RecruitmentDraftInfo.ScheduleInfo(
                applyStart,
                applyEnd,
                docResult,
                interviewStart,
                interviewEnd,
                finalResult,
                interviewTimeTableInfo
        );
    }

    private RecruitmentDraftInfo.InterviewTimeTableInfo parseInterviewTimeTable(
            Map<String, Object> interviewTimeTable) {
        if (interviewTimeTable == null) {
            return null;
        }

        try {
            Enabled raw = objectMapper.convertValue(interviewTimeTable, Enabled.class);

            List<RecruitmentDraftInfo.TimesByDateInfo> disabledByDate =
                    InterviewTimeTableDisabledCalculator.calculateDisabled(
                            raw.dateRange(),
                            raw.timeRange(),
                            raw.slotMinutes(),
                            raw.enabledByDate()
                    );

            return new RecruitmentDraftInfo.InterviewTimeTableInfo(
                    raw.dateRange(),
                    raw.timeRange(),
                    raw.slotMinutes(),
                    raw.enabledByDate(),
                    disabledByDate
            );
        } catch (Exception e) {
            throw new IllegalStateException("Invalid interviewTimeTable", e);
        }
    }

    private record Enabled(
            RecruitmentDraftInfo.DateRangeInfo dateRange,
            RecruitmentDraftInfo.TimeRangeInfo timeRange,
            Integer slotMinutes,
            List<RecruitmentDraftInfo.TimesByDateInfo> enabledByDate
    ) {
    }
}
