package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.adapter.out.ApplicationQueryRepository;
import com.umc.product.recruitment.adapter.out.dto.ApplicationIdWithFormResponseId;
import com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAssignmentRow;
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
import com.umc.product.recruitment.application.port.out.LoadInterviewSlotPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.InterviewSlot;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import com.umc.product.survey.application.port.out.LoadSingleAnswerPort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final LoadInterviewSlotPort loadInterviewSlotPort;
    private final LoadSingleAnswerPort loadSingleAnswerPort;
    private final ApplicationQueryRepository applicationQueryRepository;

    @Override
    public InterviewSchedulingSummaryInfo get(GetInterviewSchedulingSummaryQuery query) {

        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_NOT_FOUND
            ));

        Long rootId = recruitment.getEffectiveRootId();

        Recruitment rootRecruitment = recruitment.isRoot() ? recruitment :
            loadRecruitmentPort.findById(rootId)
                .orElseThrow(() -> new BusinessException(
                    Domain.RECRUITMENT,
                    RecruitmentErrorCode.ROOT_RECRUITMENT_NOT_FOUND
                ));

        PartOption requestedPart = (query.part() != null) ? query.part() : PartOption.ALL;

        // todo: 운영진 권한 검증 필요
        // interview window 로드 (dateOptions / default date)
        // TODO: LoadRecruitmentSchedulePort 반환 타입을 Optional로 변경 검토
        //       (현재는 nullable 반환을 가정하고 null 체크로 처리)
        RecruitmentSchedule window = loadRecruitmentSchedulePort
            .findByRecruitmentIdAndType(rootId, RecruitmentScheduleType.INTERVIEW_WINDOW);

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
        RulesParsed rulesParsed = parseRulesFromTimeTable(rootRecruitment.getInterviewTimeTable());

        // progress
        InterviewSchedulingSummaryInfo.ProgressInfo progress =
            (requestedPart == PartOption.ALL)
                ? buildProgressAll(rootId)
                : buildProgressPart(rootId, requestedPart);

        // partOptions (done은 contextDate 기준)
        List<InterviewSchedulingSummaryInfo.PartOptionInfo> partOptions =
            buildPartOptions(rootId, contextDate);

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

        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        PartOption requestedPart = (query.part() != null) ? query.part() : PartOption.ALL;

        // contextDate 기본값: interview window start
        RecruitmentSchedule window = loadRecruitmentSchedulePort
            .findByRecruitmentIdAndType(rootId, RecruitmentScheduleType.INTERVIEW_WINDOW);
        if (window == null || window.getStartsAt() == null || window.getEndsAt() == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_WINDOW_NOT_SET);
        }

        LocalDate windowStart = toKstLocalDate(window.getStartsAt());
        LocalDate contextDate = (query.date() != null) ? query.date() : windowStart;

        // 해당 날짜 slot 목록 조회 (KST LocalDate -> Instant)
        Instant dayStart = contextDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant dayEnd = contextDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();

        List<InterviewSlot> slots = loadInterviewSlotPort.findByRootIdAndStartsAtBetween(
            rootId, dayStart, dayEnd
        );

        // slot이 없으면 빈 응답
        if (slots.isEmpty()) {
            return new InterviewSchedulingSlotsInfo(contextDate.toString(), requestedPart.name(), List.of());
        }

        // 모집에 속한 applicationId + formResponseId 조회 (formResponse 경유)
        // projection: (applicationId, formResponseId)
        List<ApplicationIdWithFormResponseId> apps = (requestedPart == PartOption.ALL)
            ? loadApplicationPort.findDocPassedApplicationIdsWithFormResponseIdsByRootId(rootId)
            : loadApplicationPort.findDocPassedApplicationIdsWithFormResponseIdsByRootIdAndFirstPreferredPart(
                rootId, requestedPart);

        if (apps.isEmpty()) {
            // 지원자가 없으면 availableCount=0, done 정책 처리
            List<InterviewSchedulingSlotsInfo.SlotInfo> slotInfos = slots.stream()
                .sorted(Comparator.comparing(InterviewSlot::getStartsAt))
                .map(s -> toSlotInfo(s, 0, true)) // 지원자가 없으면 true 처리 (기획 문의)
                .toList();

            return new InterviewSchedulingSlotsInfo(contextDate.toString(), requestedPart.name(), slotInfos);
        }

        Map<Long, Long> formResponseIdByAppId = apps.stream()
            .collect(java.util.stream.Collectors.toMap(ApplicationIdWithFormResponseId::applicationId,
                ApplicationIdWithFormResponseId::formResponseId));
        List<Long> formResponseIds = apps.stream()
            .map(ApplicationIdWithFormResponseId::formResponseId)
            .distinct()
            .toList();

        // SCHEDULE 답변 일괄 조회: formResponseId -> scheduleValue(Map)
        Map<Long, Map<String, Object>> scheduleValueByFormResponseId =
            loadSingleAnswerPort.findScheduleValuesByFormResponseIds(formResponseIds);

        // assigned applicationId set (recruitment 전체 기준)
        Set<Long> assignedAppIds = loadInterviewAssignmentPort.findAssignedApplicationIdsByRootId(rootId);

        // slot별 voters 집계 및 done 계산
        String dateStr = contextDate.toString();

        List<InterviewSchedulingSlotsInfo.SlotInfo> result = slots.stream()
            .sorted(Comparator.comparing(InterviewSlot::getStartsAt))
            .map(slot -> {
                String startHHmm = slot.getStartsAt().atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                String endHHmm = slot.getEndsAt().atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

                // voters: 이 slot에 가능 투표한 appIds
                int availableCount = 0;
                boolean allVotersAssigned = true;
                boolean hasVoter = false;

                for (var e : formResponseIdByAppId.entrySet()) {
                    Long appId = e.getKey();
                    Long frId = e.getValue();

                    Map<String, Object> scheduleValue = scheduleValueByFormResponseId.get(frId);
                    if (scheduleValue == null || scheduleValue.isEmpty()) {
                        continue;
                    }

                    if (isVoted(scheduleValue, dateStr, startHHmm)) {
                        hasVoter = true;
                        availableCount++;
                        if (!assignedAppIds.contains(appId)) {
                            allVotersAssigned = false;
                        }
                    }
                }

                boolean done;
                if (!hasVoter) {
                    done = true; // 해당 슬롯에 가능하다고 투표한 지원자가 없으면 true 처리 (기획 문의)
                } else {
                    done = allVotersAssigned;
                }

                return new InterviewSchedulingSlotsInfo.SlotInfo(
                    slot.getId(),
                    startHHmm,
                    endHHmm,
                    availableCount,
                    done
                );
            })
            .toList();

        return new InterviewSchedulingSlotsInfo(contextDate.toString(), requestedPart.name(), result);
    }

    @Override
    public InterviewSchedulingApplicantsInfo get(GetInterviewSchedulingApplicantsQuery query) {
        // todo: 운영진 권한 검증 필요

        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        Long slotId = query.slotId();
        PartOption requestedPart = (query.part() != null) ? query.part() : PartOption.ALL;
        String keyword = query.keyword();

        // slot 검증
        InterviewSlot slot = loadInterviewSlotPort.findById(slotId)
            .orElseThrow(() -> new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.INTERVIEW_SLOT_NOT_FOUND
            ));

        if (!slot.getRecruitment().getEffectiveRootId().equals(rootId)) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.INTERVIEW_SLOT_NOT_IN_RECRUITMENT
            );
        }

        // slot time (KST)
        ZoneId KST = ZoneId.of("Asia/Seoul");
        String slotDate = slot.getStartsAt().atZone(KST).toLocalDate().toString();
        String slotStartHHmm = slot.getStartsAt().atZone(KST).toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"));
        String slotEndHHmm = slot.getEndsAt().atZone(KST).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        // DOC_PASSED 지원자(appId, formResponseId) 조회 (part 1지망 필터 적용)
        List<ApplicationIdWithFormResponseId> apps = (requestedPart == PartOption.ALL)
            ? loadApplicationPort.findDocPassedApplicationIdsWithFormResponseIdsByRootId(rootId)
            : loadApplicationPort.findDocPassedApplicationIdsWithFormResponseIdsByRootIdAndFirstPreferredPart(
                rootId, requestedPart);

        if (apps.isEmpty()) {
            return new InterviewSchedulingApplicantsInfo(List.of(), List.of());
        }

        Map<Long, Long> formResponseIdByAppId = apps.stream()
            .collect(java.util.stream.Collectors.toMap(
                ApplicationIdWithFormResponseId::applicationId,
                ApplicationIdWithFormResponseId::formResponseId
            ));

        List<Long> formResponseIds = apps.stream()
            .map(ApplicationIdWithFormResponseId::formResponseId)
            .distinct()
            .toList();

        // scheduleValue 로드 후, 이 slot에 투표한 지원자만 추림
        Map<Long, Map<String, Object>> scheduleValueByFormResponseId =
            loadSingleAnswerPort.findScheduleValuesByFormResponseIds(formResponseIds);

        Set<Long> votedAppIds = formResponseIdByAppId.entrySet().stream()
            .filter(e -> {
                Long appId = e.getKey();
                Long frId = e.getValue();
                Map<String, Object> scheduleValue = scheduleValueByFormResponseId.get(frId);
                if (scheduleValue == null || scheduleValue.isEmpty()) {
                    return false;
                }
                return isVoted(scheduleValue, slotDate, slotStartHHmm);
            })
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());

        if (votedAppIds.isEmpty()) {
            return new InterviewSchedulingApplicantsInfo(List.of(), List.of());
        }

        // 배정 여부로 분리 (현재 slot 배정자는 alreadyScheduled에 포함시키지 않음: repo에서 slot != slotId)
        Set<Long> assignedAppIds = loadInterviewAssignmentPort.findAssignedApplicationIdsByRootId(rootId);

        Set<Long> availableAppIds = votedAppIds.stream()
            .filter(appId -> !assignedAppIds.contains(appId))
            .collect(java.util.stream.Collectors.toSet());

        Set<Long> alreadyScheduledAppIds = votedAppIds.stream()
            .filter(assignedAppIds::contains)
            .collect(java.util.stream.Collectors.toSet());

        // docScore 평균 (둘 합친 appIds 기준)
        Set<Long> targetAppIds = new java.util.HashSet<Long>();
        targetAppIds.addAll(availableAppIds);
        targetAppIds.addAll(alreadyScheduledAppIds);

        Map<Long, Double> docScoreByApplicationId =
            loadApplicationPort.findAvgDocumentScoresByApplicationIds(targetAppIds);

        // rows 조회 (keyword 적용)
        List<com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAvailableApplicantRow> availableRows =
            availableAppIds.isEmpty() ? List.of() :
                applicationQueryRepository.findAvailableRows(rootId, availableAppIds, requestedPart, keyword);

        List<com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAlreadyScheduledApplicantRow> alreadyRows =
            alreadyScheduledAppIds.isEmpty() ? List.of() :
                applicationQueryRepository.findAlreadyScheduledRows(rootId, slotId, alreadyScheduledAppIds,
                    requestedPart, keyword);

        // Info 매핑
        List<InterviewSchedulingApplicantsInfo.AvailableApplicantInfo> available = availableRows.stream()
            .map(r -> new InterviewSchedulingApplicantsInfo.AvailableApplicantInfo(
                r.applicationId(),
                r.nickname(),
                r.name(),
                toPartOption(r.firstPart()),
                toPartOption(r.secondPart()),
                docScoreByApplicationId.get(r.applicationId())
            ))
            .toList();

        List<InterviewSchedulingApplicantsInfo.AlreadyScheduledApplicantInfo> alreadyScheduled = alreadyRows.stream()
            .map(r -> new InterviewSchedulingApplicantsInfo.AlreadyScheduledApplicantInfo(
                r.applicationId(),
                r.assignmentId(),
                r.nickname(),
                r.name(),
                toPartOption(r.firstPart()),
                toPartOption(r.secondPart()),
                docScoreByApplicationId.get(r.applicationId()),
                new InterviewSchedulingApplicantsInfo.AlreadyScheduledApplicantInfo.ScheduledSlotInfo(
                    r.slotStartsAt().atZone(KST).toLocalDate().toString(),
                    r.slotStartsAt().atZone(KST).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    r.slotEndsAt().atZone(KST).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                )
            ))
            .toList();

        return new InterviewSchedulingApplicantsInfo(available, alreadyScheduled);

    }

    @Override
    public InterviewSchedulingAssignmentsInfo get(GetInterviewSchedulingAssignmentsQuery query) {
        // todo: 운영진 권한 검증 필요
        Long recruitmentId = query.recruitmentId();
        Long slotId = query.slotId();
        PartOption requestedPart = (query.part() != null) ? query.part() : PartOption.ALL;

        // slot이 recruitment에 속하는지 검증
        InterviewSlot slot = loadInterviewSlotPort.findById(slotId)
            .orElseThrow(() -> new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.INTERVIEW_SLOT_NOT_FOUND
            ));

        if (!slot.getRecruitment().getId().equals(recruitmentId)) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.INTERVIEW_SLOT_NOT_IN_RECRUITMENT
            );
        }

        // 해당 슬롯에 배정된 assignment 목록 조회 (배정 순서대로)
        //    - 여기서는 docScore 제외한 정보만 가져옴
        //    - part도 ChallengerPart로 가져와서 서비스에서 매핑
        List<InterviewSchedulingAssignmentRow> rows =
            loadInterviewAssignmentPort.findAssignmentRowsByRecruitmentIdAndSlotId(
                recruitmentId,
                slotId,
                requestedPart
            );

        if (rows.isEmpty()) {
            return new InterviewSchedulingAssignmentsInfo(List.of());
        }

        Set<Long> applicationIds = rows.stream()
            .map(InterviewSchedulingAssignmentRow::applicationId)
            .collect(java.util.stream.Collectors.toSet());

        Map<Long, Double> docScoreByApplicationId =
            loadApplicationPort.findAvgDocumentScoresByApplicationIds(applicationIds);

        List<InterviewSchedulingAssignmentsInfo.InterviewAssignmentInfo> result = rows.stream()
            .map(r -> new InterviewSchedulingAssignmentsInfo.InterviewAssignmentInfo(
                r.assignmentId(),
                r.applicationId(),
                r.nickname(),
                r.name(),
                toPartOption(r.firstPart()),
                toPartOption(r.secondPart()),
                docScoreByApplicationId.get(r.applicationId())
            ))
            .toList();

        return new InterviewSchedulingAssignmentsInfo(result);
    }

    // 본모집 + 모든 추가모집의 진행률 합산
    private InterviewSchedulingSummaryInfo.ProgressInfo buildProgressAll(Long rootId) {
        long total = loadApplicationPort.countDocPassedByRootId(rootId);
        long scheduled = loadInterviewAssignmentPort.countByRootId(rootId);

        return new InterviewSchedulingSummaryInfo.ProgressInfo("ALL", "ALL", total, scheduled);
    }

    private InterviewSchedulingSummaryInfo.ProgressInfo buildProgressPart(Long rootId, PartOption part) {
        long total = loadApplicationPort.countDocPassedByRootIdAndFirstPreferredPart(rootId, part);
        long scheduled = loadInterviewAssignmentPort.countByRootIdAndFirstPreferredPart(rootId, part);

        return new InterviewSchedulingSummaryInfo.ProgressInfo("PART", part.name(), total, scheduled);
    }

    private List<InterviewSchedulingSummaryInfo.PartOptionInfo> buildPartOptions(Long rootId, LocalDate date) {
        List<InterviewSchedulingSummaryInfo.PartOptionInfo> result = new ArrayList<>();

        // ALL은 항상 포함
        result.add(new InterviewSchedulingSummaryInfo.PartOptionInfo(
            PartOption.ALL.getCode(),
            PartOption.ALL.getLabel(),
            false // TODO: ALL done 정책 확정 필요. 현재는 체크 미노출
        ));

        // OPEN 파트만 포함
        List<ChallengerPart> openParts = loadRecruitmentPartPort.findOpenPartsByRootId(rootId);

        for (ChallengerPart cp : openParts) {
            PartOption p = PartOption.valueOf(cp.name());
            long total = loadApplicationPort.countDocPassedByRootIdAndFirstPreferredPart(rootId, p);
            long scheduledOnDate = loadInterviewAssignmentPort
                .countByRootIdAndDateAndFirstPreferredPart(rootId, date, p);

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

    // 지원자가 없는 경우 사용
    private InterviewSchedulingSlotsInfo.SlotInfo toSlotInfo(InterviewSlot slot, int availableCount, boolean done) {
        String startHHmm = slot.getStartsAt().atZone(ZoneId.of("Asia/Seoul"))
            .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String endHHmm = slot.getEndsAt().atZone(ZoneId.of("Asia/Seoul"))
            .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        return new InterviewSchedulingSlotsInfo.SlotInfo(slot.getId(), startHHmm, endHHmm, availableCount, done);
    }

    // 지원자 답변에서 selected 항목을 파싱하여 해당 시간에 투표했는지 확인
    @SuppressWarnings("unchecked")
    private boolean isVoted(Map<String, Object> scheduleValue, String date, String startHHmm) {
        Object selectedRaw = scheduleValue.get("selected");
        if (!(selectedRaw instanceof List<?> selectedList)) {
            return false;
        }

        for (Object itemObj : selectedList) {
            if (!(itemObj instanceof Map<?, ?> item)) {
                continue;
            }

            Object dateObj = item.get("date");
            if (dateObj == null) {
                continue;
            }
            if (!date.equals(String.valueOf(dateObj))) {
                continue;
            }

            Object timesRaw = item.get("times");
            if (!(timesRaw instanceof List<?> timesList)) {
                return false;
            }

            for (Object t : timesList) {
                if (t == null) {
                    continue;
                }
                if (startHHmm.equals(String.valueOf(t).trim())) {
                    return true;
                }
            }
            return false; // 해당 date entry를 찾았는데 times에 없으면 false
        }

        return false;
    }

    private PartOption toPartOption(com.umc.product.common.domain.enums.ChallengerPart part) {
        if (part == null) {
            return null;
        }
        return PartOption.valueOf(part.name());
    }

}
