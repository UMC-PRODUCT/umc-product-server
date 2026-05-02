package com.umc.product.survey.application.service.command;

import com.umc.product.survey.application.port.in.command.ManageFormUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormCommand;
import com.umc.product.survey.application.port.in.command.dto.PublishFormCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormCommand;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.SaveAnswerPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.application.port.out.SaveFormResponsePort;
import com.umc.product.survey.application.port.out.SaveFormSectionPort;
import com.umc.product.survey.application.port.out.SaveQuestionOptionPort;
import com.umc.product.survey.application.port.out.SaveQuestionPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FormCommandService implements ManageFormUseCase {

    private final LoadFormPort loadFormPort;
    private final SaveFormPort saveFormPort;
    private final SaveFormSectionPort saveFormSectionPort;
    private final SaveQuestionPort saveQuestionPort;
    private final SaveQuestionOptionPort saveQuestionOptionPort;
    private final SaveFormResponsePort saveFormResponsePort;
    private final SaveAnswerPort saveAnswerPort;

    @Override
    public Long createDraft(CreateDraftFormCommand command) {
        Form form = Form.createDraft(command.title(), command.createdMemberId());

        return saveFormPort.save(form).getId();
    }

    @Override
    public void updateForm(UpdateFormCommand command) {
        Form form = loadFormPort.findById(command.formId())
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));

        form.update(command.title(), command.description(), command.isAnonymous());
        saveFormPort.save(form);
    }

    @Override
    public void publishForm(PublishFormCommand command) {
        Form form = loadFormPort.findById(command.formId())
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));

        form.publish();
        saveFormPort.save(form);
    }

    @Override
    public void deleteForm(DeleteFormCommand command) {
        Long formId = command.formId();

        // 응답 트리 (자식부터)
        saveAnswerPort.deleteByFormId(formId);
        saveFormResponsePort.deleteByFormId(formId);

        // 폼 구조 (자식부터)
        saveQuestionOptionPort.deleteByFormId(formId);
        saveQuestionPort.deleteByFormId(formId);
        saveFormSectionPort.deleteByFormId(formId);

        // 폼 본체
        saveFormPort.deleteById(formId);
    }
}
