package com.umc.product.survey.application.service;

import com.umc.product.survey.application.port.in.command.ManageVoteUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;
import com.umc.product.survey.application.port.out.*;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VoteService implements ManageVoteUseCase {

    private final SaveFormPort saveFormPort;
    private final LoadFormPort loadFormPort;
    private final SaveFormSectionPort saveFormSectionPort;
    private final SaveQuestionPort saveQuestionPort;
    private final SaveQuestionOptionPort saveQuestionOptionPort;

    @Override
    public Long createVote(CreateVoteCommand command) {
        QuestionType qType = command.allowMultipleChoice()
            ? QuestionType.CHECKBOX
            : QuestionType.RADIO;

        // 1. 순수한 Form 생성 (상태는 무조건 PUBLISHED)
        Form form = Form.createPublished(command.createdMemberId(), command.title(), command.isAnonymous());
        Form savedForm = saveFormPort.save(form);

        // 2. 단일 섹션 생성
        FormSection section = FormSection.builder()
            .form(savedForm)
            .title(command.title())
            .orderNo(1L)
            .build();
        FormSection savedSection = saveFormSectionPort.save(section);

        // 3. 단일 질문 생성
        Question question = Question.create(
            command.title(),
            qType,
            true, // isRequired
            1L    // orderNo
        );
        question.assignTo(savedSection);
        Question savedQuestion = saveQuestionPort.save(question);

        // 4. 질문에 대한 선택지(옵션) 생성
        AtomicInteger order = new AtomicInteger(1);
        List<QuestionOption> options = command.options().stream()
            .map(optContent -> {
                QuestionOption opt = QuestionOption.create(optContent, order.getAndIncrement(), false);
                opt.assignTo(savedQuestion);
                return opt;
            })
            .collect(Collectors.toList());
        saveQuestionOptionPort.saveAll(options);

        // 조립된 Form의 ID 반환 (이 ID를 NoticeVote가 voteId라는 이름으로 가집니다)
        return savedForm.getId();
    }

    @Override
    public void deleteVote(Long formId) {
        Form form = loadFormPort.findById(formId)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));

        // TODO: 향후 Cascade 정책 혹은 응답(FormResponse, Answer) 삭제 로직 보완 필요

        saveFormPort.deleteById(form.getId());
    }
}
