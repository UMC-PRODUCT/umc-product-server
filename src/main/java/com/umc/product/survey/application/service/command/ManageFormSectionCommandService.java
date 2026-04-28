package com.umc.product.survey.application.service.command;

import com.umc.product.survey.application.port.in.command.ManageFormSectionUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateFormSectionCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormSectionCommand;
import com.umc.product.survey.application.port.in.command.dto.ReorderFormSectionsCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormSectionCommand;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadFormSectionPort;
import com.umc.product.survey.application.port.out.SaveFormSectionPort;
import com.umc.product.survey.application.port.out.SaveQuestionOptionPort;
import com.umc.product.survey.application.port.out.SaveQuestionPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageFormSectionCommandService implements ManageFormSectionUseCase {

    private final LoadFormPort loadFormPort;
    private final LoadFormSectionPort loadFormSectionPort;
    private final SaveFormSectionPort saveFormSectionPort;
    private final SaveQuestionPort saveQuestionPort;
    private final SaveQuestionOptionPort saveQuestionOptionPort;

    @Override
    public Long createSection(CreateFormSectionCommand command) {
        Form form = loadFormPort.findById(command.formId())
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));

        long nextOrderNo = loadFormSectionPort.listByFormId(command.formId()).stream()
            .mapToLong(FormSection::getOrderNo)
            .max()
            .orElse(0L) + 1L;

        FormSection section = FormSection.create(form, command.title(), command.description(), nextOrderNo);
        return saveFormSectionPort.save(section).getId();
    }

    @Override
    public void updateSection(UpdateFormSectionCommand command) {
        FormSection section = loadFormSectionPort.findById(command.sectionId())
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));

        section.update(command.title(), command.description());
        saveFormSectionPort.save(section);
    }

    @Override
    public void deleteSection(DeleteFormSectionCommand command) {
        Long sectionId = command.sectionId();

        // cascade (자식부터)
        saveQuestionOptionPort.deleteBySectionId(sectionId);
        saveQuestionPort.deleteBySectionId(sectionId);
        saveFormSectionPort.deleteById(sectionId);
    }

    @Override
    public void reorderSections(ReorderFormSectionsCommand command) {
        List<FormSection> sections = loadFormSectionPort.listByFormId(command.formId());

        Set<Long> existingIds = sections.stream()
            .map(FormSection::getId)
            .collect(Collectors.toSet());
        Set<Long> requestedIds = new HashSet<>(command.orderedSectionIds());

        if (!existingIds.equals(requestedIds)) {
            throw new SurveyDomainException(
                SurveyErrorCode.INVALID_VOTE_FORM_STRUCTURE,
                "재배치 요청의 섹션 ID 셋이 실제 폼의 섹션 ID 셋과 일치하지 않습니다."
            );
        }

        Map<Long, FormSection> byId = sections.stream()
            .collect(Collectors.toMap(FormSection::getId, Function.identity()));

        for (int i = 0; i < command.orderedSectionIds().size(); i++) {
            byId.get(command.orderedSectionIds().get(i)).updateOrderNo((long) (i + 1));
        }

        saveFormSectionPort.saveAll(sections);
    }
}
