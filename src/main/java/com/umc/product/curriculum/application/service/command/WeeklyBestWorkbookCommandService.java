package com.umc.product.curriculum.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.curriculum.application.port.in.command.ManageWeeklyBestWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateWeeklyBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditWeeklyBestWorkbookCommand;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.exception.constant.Domain;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WeeklyBestWorkbookCommandService implements ManageWeeklyBestWorkbookUseCase {

    @Audited(
        domain = Domain.CURRICULUM,
        action = AuditAction.APPROVE,
        targetType = "WeeklyBestWorkbook",
        description = "'주간 베스트 워크북을 선정했습니다.'"
    )
    @Override
    public void selectBest(CreateWeeklyBestWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editReason(EditWeeklyBestWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void withdraw(Long weeklyBestWorkbookId) {
        throw new NotImplementedException();
    }
}
