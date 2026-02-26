package com.umc.product.schedule.application.service.command;

import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.schedule.application.port.in.command.CreateScheduleWithAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CreateStudyGroupScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateStudyGroupScheduleCommand;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupScheduleCommandService implements CreateStudyGroupScheduleUseCase {

    private final GetStudyGroupUseCase getStudyGroupUseCase;
    private final CreateScheduleWithAttendanceUseCase createScheduleWithAttendanceUseCase;
    private final LoadSchedulePort loadSchedulePort;

    @Override
    public Long create(CreateStudyGroupScheduleCommand command) {
        StudyGroupDetailInfo studyGroup = getStudyGroupUseCase.getStudyGroupDetail(command.studyGroupId());

        List<Long> participantMemberIds = new ArrayList<>();
        participantMemberIds.add(studyGroup.leader().memberId());
        for (StudyGroupDetailInfo.MemberInfo member : studyGroup.members()) {
            participantMemberIds.add(member.memberId());
        }

        Long scheduleId = createScheduleWithAttendanceUseCase.create(
            command.toScheduleWithAttendanceCommand(participantMemberIds)
        );

        Schedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        schedule.assignStudyGroup(command.studyGroupId());

        return scheduleId;
    }
}
