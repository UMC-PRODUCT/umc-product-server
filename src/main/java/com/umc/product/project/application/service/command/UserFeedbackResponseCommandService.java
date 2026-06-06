package com.umc.product.project.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.project.application.port.in.command.SubmitUserFeedbackResponseUseCase;
import com.umc.product.project.application.port.in.command.dto.SubmitUserFeedbackResponseCommand;
import com.umc.product.project.application.port.out.LoadUserFeedbackTemplatePort;
import com.umc.product.project.domain.UserFeedbackTemplate;
import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import com.umc.product.survey.application.port.in.command.dto.SubmitFormResponseCommand;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFeedbackResponseCommandService implements SubmitUserFeedbackResponseUseCase {

    private final LoadUserFeedbackTemplatePort loadUserFeedbackTemplatePort;
    private final ManageFormResponseUseCase manageFormResponseUseCase;

    @Override
    public Long submit(SubmitUserFeedbackResponseCommand command) {
        UserFeedbackTemplate template = loadUserFeedbackTemplatePort.getById(command.templateId());

        return manageFormResponseUseCase.submitImmediately(
            SubmitFormResponseCommand.builder()
                .formId(template.getFormId())
                .respondentMemberId(command.respondentMemberId())
                .answers(command.answers())
                .build()
        );
    }
}
