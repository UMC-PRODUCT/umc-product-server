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
import com.umc.product.schedule.application.port.in.query.dto.ScheduleBaseInfo;
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
        loadStudyGroupPort.getById(command.studyGroupId());

        // schedule 존재 여부 및 출석 정책 필수 여부 확인
        ScheduleBaseInfo scheduleInfo = getScheduleUseCase.getScheduleBaseInfo(command.scheduleId());

        if (!scheduleInfo.isAttendanceChecked()) {
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
