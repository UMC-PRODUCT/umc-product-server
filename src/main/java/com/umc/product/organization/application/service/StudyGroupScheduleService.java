package com.umc.product.organization.application.service;

import com.umc.product.curriculum.application.port.in.query.GetWeeklyCurriculumUseCase;
import com.umc.product.organization.application.port.in.command.CreateStudyGroupScheduleUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupScheduleCommand;
import com.umc.product.organization.application.port.out.command.SaveStudyGroupSchedulePort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.StudyGroupSchedule;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.schedule.application.port.in.query.GetScheduleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupScheduleService implements CreateStudyGroupScheduleUseCase {

    private final SaveStudyGroupSchedulePort saveStudyGroupSchedulePort;
    private final LoadStudyGroupPort loadStudyGroupPort;

    // 외부 useCase
    private final GetScheduleUseCase getScheduleUseCase;
    private final GetWeeklyCurriculumUseCase getWeeklyCurriculumUseCase;


    @Override
    public Long create(CreateStudyGroupScheduleCommand command) {

        // studyGroup 없으면 에러 반환
        loadStudyGroupPort.findById(command.studyGroupId());

        // schedule 없으면 에러 반환
        getScheduleUseCase.getScheduleBaseInfo(command.scheduleId());

        // 출석 정책 없으면 에러 반환
        if (!getScheduleUseCase.hasAttendancePolicy(command.scheduleId())) {
            throw new OrganizationDomainException(
                OrganizationErrorCode.STUDY_GROUP_SCHEDULE_ATTENDANCE_POLICY_REQUIRED);
        }

        // weeklyCurriculum 없으면 에러 반환
        getWeeklyCurriculumUseCase.getWeeklyCurriculum(command.weeklyCurriculumId());

        // StudyGroupSchedule 생성
        StudyGroupSchedule studyGroupSchedule = saveStudyGroupSchedulePort.save(command.toEntity());

        return studyGroupSchedule.getId();
    }
}
