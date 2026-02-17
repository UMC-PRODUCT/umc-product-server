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
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewAssignmentPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.InterviewSlot;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import jakarta.transaction.Transactional;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Transactional
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
    private final LoadRecruitmentPort loadRecruitmentPort;

    @Override
    public CreateInterviewAssignmentResult create(CreateInterviewAssignmentCommand command) {
        // todo: 운영진 권한 검증 필요
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        Long applicationId = command.applicationId();

        InterviewSlot slot = loadInterviewSlotPort.findById(command.slotId())
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_SLOT_NOT_FOUND));

        if (!slot.getRecruitment().getEffectiveRootId().equals(rootId)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_SLOT_NOT_IN_RECRUITMENT);
        }

        Application application = loadApplicationPort.findById(command.applicationId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.APPLICATION_NOT_FOUND));

        if (!loadApplicationListPort.isApplicationBelongsToRecruitmentFamily(application.getId(), rootId)) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT);
        }

        if (application.getStatus() != ApplicationStatus.DOC_PASSED) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_ONLY_DOC_PASSED);
        }

        if (loadInterviewAssignmentPort.existsByRootIdAndApplicationId(rootId, application.getId())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_ALREADY_EXISTS);
        }

        InterviewAssignment saved = saveInterviewAssignmentPort.save(
            InterviewAssignment.create(slot.getRecruitment(), application, slot)
        );

        // summary는 화면 컨텍스트(date/part) 기준으로 계산
        var date = command.date(); // null 가능 (전체)
        var part = command.part() == null ? PartOption.ALL : command.part();

        var summary = getInterviewSchedulingSummaryUseCase.get(
            new GetInterviewSchedulingSummaryQuery(command.recruitmentId(), date, part, command.requesterId()));

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
        InterviewAssignment assignment =
            loadInterviewAssignmentPort.findById(command.assignmentId())
                .orElseThrow(() -> new BusinessException(
                    Domain.RECRUITMENT,
                    RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND
                ));

        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        if (!assignment.getRecruitment().getEffectiveRootId().equals(rootId)) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_IN_RECRUITMENT
            );
        }

        Long applicationId = assignment.getApplication().getId();

        // 삭제
        saveInterviewAssignmentPort.delete(assignment);

        // summary 재계산
        var date = command.date(); // null 가능
        var part = command.part() == null ? PartOption.ALL : command.part();

        var summary = getInterviewSchedulingSummaryUseCase.get(
            new GetInterviewSchedulingSummaryQuery(
                command.recruitmentId(),
                date,
                part,
                command.requesterId()
            )
        );

        return new DeleteInterviewAssignmentResult(
            new DeleteInterviewAssignmentResult.UnassignedInfo(applicationId),
            summary
        );
    }

}
