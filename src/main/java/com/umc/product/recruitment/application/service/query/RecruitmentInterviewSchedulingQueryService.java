package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingApplicantsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingAssignmentsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingSlotsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase.GetInterviewSchedulingSummaryUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingApplicantsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingAssignmentsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSlotsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSummaryQuery;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingApplicantsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingAssignmentsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSlotsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSummaryInfo;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentInterviewSchedulingQueryService implements GetInterviewSchedulingSummaryUseCase,
    GetInterviewSchedulingSlotsUseCase,
    GetInterviewSchedulingApplicantsUseCase,
    GetInterviewSchedulingAssignmentsUseCase {

    private final LoadRecruitmentSchedulePort loadRecruitmentSchedulePort;
    private final LoadRecruitmentPort loadRecruitmentPort;
    private final LoadApplicationPort loadApplicationPort;
    private final LoadInterviewAssignmentPort loadInterviewAssignmentPort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;

    @Override
    public InterviewSchedulingSummaryInfo get(GetInterviewSchedulingSummaryQuery query) {
        Long recruitmentId = query.recruitmentId();
        PartOption requestedPart = (query.part() != null) ? query.part() : PartOption.ALL;

        // interview window 로드 (dateOptions / default date)
        // TODO: LoadRecruitmentSchedulePort 반환 타입을 Optional로 변경 검토
        //       (현재는 nullable 반환을 가정하고 null 체크로 처리)
        RecruitmentSchedule window = loadRecruitmentSchedulePort
            .findByRecruitmentIdAndType(recruitmentId, RecruitmentScheduleType.INTERVIEW_WINDOW);

        if (window == null) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_SCHEDULE_NOT_FOUND
            );
        }

        if (window.getStartsAt() == null || window.getEndsAt() == null) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.INTERVIEW_WINDOW_NOT_SET
            );
        }

        LocalDate windowStart = toKstLocalDate(window.getStartsAt());
        LocalDate windowEnd = toKstLocalDate(window.getEndsAt());

        // date 기본값
        LocalDate contextDate = (query.date() != null) ? query.date() : windowStart;

        // dateOptions
        List<LocalDate> dateOptions = datesBetweenInclusive(windowStart, windowEnd);

        // rules: recruitment.interviewTimeTable에서 추출
        Recruitment recruitment = loadRecruitmentPort.findById(recruitmentId)
            .orElseThrow(() -> new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_NOT_FOUND
            ));

        RulesParsed rulesParsed = parseRulesFromTimeTable(recruitment.getInterviewTimeTable());

        // progress
        InterviewSchedulingSummaryInfo.ProgressInfo progress =
            (requestedPart == PartOption.ALL)
                ? buildProgressAll(recruitmentId)
                : buildProgressPart(recruitmentId, requestedPart);

        // partOptions (done은 contextDate 기준)
        List<InterviewSchedulingSummaryInfo.PartOptionInfo> partOptions =
            buildPartOptions(recruitmentId, contextDate);

        // context
        InterviewSchedulingSummaryInfo.ContextInfo context =
            new InterviewSchedulingSummaryInfo.ContextInfo(contextDate.toString(), requestedPart.name());

        // assemble
        InterviewSchedulingSummaryInfo.RulesInfo rules = new InterviewSchedulingSummaryInfo.RulesInfo(
            rulesParsed.slotMinutes(),
            new InterviewSchedulingSummaryInfo.TimeRangeInfo(rulesParsed.startHHmm(), rulesParsed.endHHmm())
        );

        return new InterviewSchedulingSummaryInfo(progress, dateOptions, partOptions, rules, context);
    }

    @Override
    public InterviewSchedulingSlotsInfo get(GetInterviewSchedulingSlotsQuery query) {
        // todo: 운영진 권한 검증 필요
        // 해당 시간 면접 가능 지원자 수는, recruitment.form.formSection.question 중 'SCHEDULE' 타입의 질문에 대한 singleAnswer의 json 값을 파싱해서 계산 필요
        // singleAnswer의 값은
        // {"selected": [{"date": "2026-01-23", "times": ["09:00", "09:30", "10:00"]}, {"date": "2026-01-24", "times": ["09:00", "09:30", "10:00"]}]}
        // 위 형식으로 되어있으며, 이 중 date와 times를 보고 해당 날짜에 가능한 시간대를 파악할 수 있습니다.
        return null;
    }

    @Override
    public InterviewSchedulingApplicantsInfo get(GetInterviewSchedulingApplicantsQuery query) {
        // todo: 운영진 권한 검증 필요
        // 해당 시간 면접 가능 지원자 목록은, recruitment.form.formSection.question 중 'SCHEDULE' 타입의 질문에 대한 singleAnswer의 json 값을 파싱해서 계산 필요
        // singleAnswer의 값은
        // {"selected": [{"date": "2026-01-23", "times": ["09:00", "09:30", "10:00"]}, {"date": "2026-01-24", "times": ["09:00", "09:30", "10:00"]}]}
        // 위 형식으로 되어있으며, 이 중 date와 times를 보고 해당 날짜에 가능한 시간대를 파악할 수 있습니다.

        return null;
    }

    @Override
    public InterviewSchedulingAssignmentsInfo get(GetInterviewSchedulingAssignmentsQuery query) {
        // todo: 운영진 권한 검증 필요
        // 해당 날짜에 면접이 배정된 지원자들의 면접 시간표를 반환

        return null;
    }

    private InterviewSchedulingSummaryInfo.ProgressInfo buildProgressAll(Long recruitmentId) {
        long total = loadApplicationPort.countByRecruitmentId(recruitmentId);
        long scheduled = loadInterviewAssignmentPort.countByRecruitmentId(recruitmentId);
        return new InterviewSchedulingSummaryInfo.ProgressInfo("ALL", "ALL", total, scheduled);
    }

    private InterviewSchedulingSummaryInfo.ProgressInfo buildProgressPart(Long recruitmentId, PartOption part) {
        long total = loadApplicationPort.countByRecruitmentIdAndFirstPreferredPart(recruitmentId, part);
        long scheduled = loadInterviewAssignmentPort.countByRecruitmentIdAndFirstPreferredPart(recruitmentId, part);
        return new InterviewSchedulingSummaryInfo.ProgressInfo("PART", part.name(), total, scheduled);
    }

    private List<InterviewSchedulingSummaryInfo.PartOptionInfo> buildPartOptions(Long recruitmentId, LocalDate date) {
        List<InterviewSchedulingSummaryInfo.PartOptionInfo> result = new ArrayList<>();

        // ALL은 항상 포함
        result.add(new InterviewSchedulingSummaryInfo.PartOptionInfo(
            PartOption.ALL.getCode(),
            PartOption.ALL.getLabel(),
            false // TODO: ALL done 정책 확정 필요. 현재는 체크 미노출
        ));

        // OPEN 파트만 포함
        List<ChallengerPart> openParts = loadRecruitmentPartPort.findOpenPartsByRecruitmentId(recruitmentId);

        for (ChallengerPart cp : openParts) {
            PartOption p = PartOption.valueOf(cp.name());
            long total = loadApplicationPort.countByRecruitmentIdAndFirstPreferredPart(recruitmentId, p);
            long scheduledOnDate = loadInterviewAssignmentPort
                .countByRecruitmentIdAndDateAndFirstPreferredPart(recruitmentId, date, p);

            boolean done = (total > 0) && (scheduledOnDate >= total);

            result.add(new InterviewSchedulingSummaryInfo.PartOptionInfo(
                p.getCode(),
                p.getLabel(),
                done
            ));
        }

        return result;
    }

    private LocalDate toKstLocalDate(Instant instant) {
        return instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();
    }

    private List<LocalDate> datesBetweenInclusive(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            // end < start일 경우, start 날짜만 반환
            return List.of(start);
        }
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d);
        }
        return dates;
    }

    /**
     * timetable 예: { "timeRange": {"start":"09:00:00","end":"23:00:00"}, "slotMinutes": 30, ... }
     */
    @SuppressWarnings("unchecked")
    private RulesParsed parseRulesFromTimeTable(Map<String, Object> timeTable) {
        if (timeTable == null || timeTable.isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }

        Object slotMinutesRaw = timeTable.get("slotMinutes");
        int slotMinutes = parseSlotMinutes(slotMinutesRaw);

        Map<String, Object> timeRange = (Map<String, Object>) timeTable.get("timeRange");
        if (timeRange == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }

        LocalTime start = LocalTime.parse(String.valueOf(timeRange.get("start")));
        LocalTime end = LocalTime.parse(String.valueOf(timeRange.get("end")));

        return new RulesParsed(slotMinutes, start.format(DateTimeFormatter.ofPattern("HH:mm")),
            end.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private int parseSlotMinutes(Object raw) {
        if (raw == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }
        try {
            if (raw instanceof Number n) {
                return n.intValue();
            }
            if (raw instanceof String s) {
                return Integer.parseInt(s.trim());
            }
            throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }
    }

    private record RulesParsed(int slotMinutes, String startHHmm, String endHHmm) {
    }
}
