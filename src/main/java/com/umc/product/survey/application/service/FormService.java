package com.umc.product.survey.application.service;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.application.port.in.command.CopyFormUseCase;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.enums.FormStatus;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormService implements CopyFormUseCase {

    private final LoadFormPort loadFormPort;
    private final SaveFormPort saveFormPort;

    @Override
    @Transactional
    public Long copyForm(Long sourceFormId, Long memberId, String newRecruitmentTitle) {

        String formTitle = (newRecruitmentTitle != null && !newRecruitmentTitle.isBlank())
            ? newRecruitmentTitle + " 지원서"
            : null; // 제목이 없으면 null

        // 1. 원본 폼 조회
        Form sourceForm = loadFormPort.findById(sourceFormId)
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));

        // 2. 새로운 폼 객체 생성 (기본 정보 복사)
        Form newForm = Form.builder()
            .createdMemberId(memberId) // 현재 생성자 ID 주입
            .title(formTitle)
            .description(sourceForm.getDescription())
            .status(FormStatus.DRAFT)
            .isAnonymous(sourceForm.isAnonymous())
            .startsAt(sourceForm.getStartsAt())
            .endsAtExclusive(sourceForm.getEndsAtExclusive())
            .build();

        // 3. 계층 구조 Copy (Section -> Question -> Option)
        for (FormSection sourceSection : sourceForm.getSections()) {
            // 섹션 복제 (Builder 활용)
            FormSection newSection = FormSection.builder()
                .form(newForm)
                .type(sourceSection.getType())
                .targetKey(sourceSection.getTargetKey())
                .title(sourceSection.getTitle())
                .description(sourceSection.getDescription())
                .orderNo(sourceSection.getOrderNo())
                .build();

            newForm.getSections().add(newSection);

            for (Question sourceQuestion : sourceSection.getQuestions()) {
                // 질문 복제
                Question newQuestion = Question.create(
                    sourceQuestion.getQuestionText(),
                    sourceQuestion.getType(),
                    sourceQuestion.getIsRequired(),
                    sourceQuestion.getOrderNo()
                );
                newSection.addQuestion(newQuestion);

                for (QuestionOption sourceOption : sourceQuestion.getOptions()) {
                    // 옵션 복제
                    QuestionOption newOption = QuestionOption.create(
                        sourceOption.getContent(),
                        sourceOption.getOrderNo(),
                        sourceOption.isOther()
                    );
                    newQuestion.addOption(newOption);
                }
            }
        }

        // 4. 저장
        Form savedForm = saveFormPort.save(newForm);

        return savedForm.getId();
    }
}
