package com.umc.product.curriculum.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.curriculum.application.port.in.command.ManageMissionSubmissionUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.DeleteMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionSubmissionCommand;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.exception.constant.Domain;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionSubmissionCommandService implements ManageMissionSubmissionUseCase {

    @Audited(
        domain = Domain.CURRICULUM,
        action = AuditAction.SUBMIT,
        targetType = "MissionSubmission",
        targetId = "#result",
        description = "'미션 제출물이 생성되었습니다.'"
    )
    @Override
    public Long create(CreateMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void edit(EditMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void withdraw(DeleteMissionSubmissionCommand command) {
        throw new NotImplementedException();
    }
}
