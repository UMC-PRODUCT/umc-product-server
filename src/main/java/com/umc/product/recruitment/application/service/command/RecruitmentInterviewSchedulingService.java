package com.umc.product.recruitment.application.service.command;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.command.CreateInterviewAssignmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteInterviewAssignmentUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentResult;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase.GetInterviewSchedulingSummaryUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.DeleteInterviewAssignmentResult;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSummaryQuery;
import com.umc.product.recruitment.application.port.out.LoadApplicationListPort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewSlotPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewAssignmentPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.InterviewSlot;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentInterviewSchedulingService implements CreateInterviewAssignmentUseCase,
    DeleteInterviewAssignmentUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final LoadInterviewSlotPort loadInterviewSlotPort;
    private final LoadApplicationPort loadApplicationPort;
    private final LoadApplicationListPort loadApplicationListPort;
    private final LoadInterviewAssignmentPort loadInterviewAssignmentPort;
    private final GetInterviewSchedulingSummaryUseCase getInterviewSchedulingSummaryUseCase;
    private final SaveInterviewAssignmentPort saveInterviewAssignmentPort;

    @Override
    public CreateInterviewAssignmentResult create(CreateInterviewAssignmentCommand command) {
        // todo: 운영진 권한 검증 필요
        Long recruitmentId = command.recruitmentId();
        Long applicationId = command.applicationId();
        Long slotId = command.slotId();

        InterviewSlot slot = loadInterviewSlotPort.findById(slotId)
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_SLOT_NOT_FOUND));

        if (!slot.getRecruitment().getId().equals(recruitmentId)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_SLOT_NOT_IN_RECRUITMENT);
        }

        Application application = loadApplicationPort.findById(applicationId)
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.APPLICATION_NOT_FOUND));

        if (!loadApplicationListPort.isApplicationBelongsToRecruitment(applicationId, recruitmentId)) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT);
        }

        if (application.getStatus() != ApplicationStatus.DOC_PASSED) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_ONLY_DOC_PASSED);
        }

        if (loadInterviewAssignmentPort.existsByRecruitmentIdAndApplicationId(recruitmentId, applicationId)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_ALREADY_EXISTS);
        }

        InterviewAssignment saved = saveInterviewAssignmentPort.save(
            InterviewAssignment.create(slot.getRecruitment(), application, slot)
        );

        // summary는 화면 컨텍스트(date/part) 기준으로 계산
        var date = command.date(); // null 가능 (전체)
        var part = command.part() == null ? PartOption.ALL : command.part();

        var summary = getInterviewSchedulingSummaryUseCase.get(
            new GetInterviewSchedulingSummaryQuery(recruitmentId, date, part, command.requesterId()));

        return new CreateInterviewAssignmentResult(
            new CreateInterviewAssignmentResult.AssignedInfo(
                saved.getId(),
                applicationId,
                new CreateInterviewAssignmentResult.AssignedInfo.SlotInfo(
                    slot.getId(),
                    slot.getStartsAt().atZone(KST).toLocalDate().format(DATE),
                    slot.getStartsAt().atZone(KST).toLocalTime().format(TIME),
                    slot.getEndsAt().atZone(KST).toLocalTime().format(TIME)
                )
            ),
            summary
        );
    }

    @Override
    public DeleteInterviewAssignmentResult delete(DeleteInterviewAssignmentCommand command) {
        // todo: 운영진 권한 검증 필요
        // 할당 해제 시 interview assignment 삭제 로직 필요
        return null;
    }

}
