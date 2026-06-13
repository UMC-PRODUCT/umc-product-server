package com.umc.product.survey.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.SubmitDraftFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.SubmitFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormResponseCommand;
import com.umc.product.survey.application.port.out.LoadAnswerPort;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.application.port.out.LoadQuestionOptionPort;
import com.umc.product.survey.application.port.out.LoadQuestionPort;
import com.umc.product.survey.application.port.out.SaveAnswerPort;
import com.umc.product.survey.application.port.out.SaveFormResponsePort;
import com.umc.product.survey.domain.Answer;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FormResponseCommandServiceTest {

    private static final Long FORM_ID = 100L;
    private static final Long MEMBER_ID = 200L;
    private static final Long FORM_RESPONSE_ID = 300L;

    @Mock
    LoadFormPort loadFormPort;
    @Mock
    LoadQuestionPort loadQuestionPort;
    @Mock
    LoadQuestionOptionPort loadQuestionOptionPort;
    @Mock
    LoadFormResponsePort loadFormResponsePort;
    @Mock
    LoadAnswerPort loadAnswerPort;
    @Mock
    SaveFormResponsePort saveFormResponsePort;
    @Mock
    SaveAnswerPort saveAnswerPort;
    @Mock
    GetFileUseCase getFileUseCase;

    @InjectMocks
    FormResponseCommandService sut;

    @Test
    @DisplayName("기본 폼은 같은 form/member의 draft 생성을 차단한다")
    void 기본_폼은_중복_draft_생성을_차단한다() {
        given(loadFormPort.findById(FORM_ID)).willReturn(Optional.of(publishedForm(false)));
        given(loadFormResponsePort.existsByFormIdAndMemberId(FORM_ID, MEMBER_ID)).willReturn(true);

        assertThatThrownBy(() -> sut.createDraft(CreateDraftFormResponseCommand.builder()
            .formId(FORM_ID)
            .respondentMemberId(MEMBER_ID)
            .build()))
            .isInstanceOf(SurveyDomainException.class)
            .extracting("baseCode")
            .isEqualTo(SurveyErrorCode.FORM_RESPONSE_ALREADY_EXISTS);

        then(saveFormResponsePort).should(never()).save(any());
    }

    @Test
    @DisplayName("기본 폼은 같은 form/member의 즉시 제출을 차단한다")
    void 기본_폼은_중복_즉시_제출을_차단한다() {
        given(loadFormPort.findById(FORM_ID)).willReturn(Optional.of(publishedForm(false)));
        given(loadFormResponsePort.existsByFormIdAndMemberId(FORM_ID, MEMBER_ID)).willReturn(true);

        assertThatThrownBy(() -> sut.submitImmediately(SubmitFormResponseCommand.builder()
            .formId(FORM_ID)
            .respondentMemberId(MEMBER_ID)
            .answers(List.of())
            .build()))
            .isInstanceOf(SurveyDomainException.class)
            .extracting("baseCode")
            .isEqualTo(SurveyErrorCode.FORM_RESPONSE_ALREADY_EXISTS);

        then(saveFormResponsePort).should(never()).save(any());
    }

    @Test
    @DisplayName("중복 허용 폼은 같은 form/member의 두 번째 draft도 새 응답으로 생성한다")
    void 중복_허용_폼은_두번째_draft도_생성한다() {
        given(loadFormPort.findById(FORM_ID)).willReturn(Optional.of(publishedForm(true)));
        given(saveFormResponsePort.save(any(FormResponse.class))).willAnswer(invocation -> {
            FormResponse response = invocation.getArgument(0);
            ReflectionTestUtils.setField(response, "id", FORM_RESPONSE_ID);
            return response;
        });

        Long result = sut.createDraft(CreateDraftFormResponseCommand.builder()
            .formId(FORM_ID)
            .respondentMemberId(MEMBER_ID)
            .build());

        assertThat(result).isEqualTo(FORM_RESPONSE_ID);
        then(loadFormResponsePort).should(never()).existsByFormIdAndMemberId(FORM_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("중복 허용 폼은 같은 form/member의 즉시 제출도 새 응답으로 생성한다")
    void 중복_허용_폼은_두번째_즉시_제출도_생성한다() {
        given(loadFormPort.findById(FORM_ID)).willReturn(Optional.of(publishedForm(true)));
        given(loadQuestionPort.listByFormId(FORM_ID)).willReturn(List.of());
        given(saveFormResponsePort.save(any(FormResponse.class))).willAnswer(invocation -> {
            FormResponse response = invocation.getArgument(0);
            ReflectionTestUtils.setField(response, "id", FORM_RESPONSE_ID);
            return response;
        });

        Long result = sut.submitImmediately(SubmitFormResponseCommand.builder()
            .formId(FORM_ID)
            .respondentMemberId(MEMBER_ID)
            .answers(List.of())
            .build());

        assertThat(result).isEqualTo(FORM_RESPONSE_ID);
        then(loadFormResponsePort).should(never()).existsByFormIdAndMemberId(FORM_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("중복 허용 폼은 formId/memberId 기반 제출 응답 수정을 막는다")
    void 중복_허용_폼은_단건_응답_수정을_막는다() {
        given(loadFormPort.findById(FORM_ID)).willReturn(Optional.of(publishedForm(true)));

        assertThatThrownBy(() -> sut.updateResponse(UpdateFormResponseCommand.builder()
            .formId(FORM_ID)
            .respondentMemberId(MEMBER_ID)
            .answers(List.of())
            .build()))
            .isInstanceOf(SurveyDomainException.class)
            .extracting("baseCode")
            .isEqualTo(SurveyErrorCode.FORM_RESPONSE_LOOKUP_AMBIGUOUS);

        then(loadFormResponsePort).should(never())
            .findSubmittedByFormIdAndRespondentMemberId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("중복 허용 폼은 formId/memberId 기반 제출 응답 삭제를 막는다")
    void 중복_허용_폼은_단건_응답_삭제를_막는다() {
        given(loadFormPort.findById(FORM_ID)).willReturn(Optional.of(publishedForm(true)));

        assertThatThrownBy(() -> sut.deleteResponse(DeleteFormResponseCommand.builder()
            .formId(FORM_ID)
            .respondentMemberId(MEMBER_ID)
            .build()))
            .isInstanceOf(SurveyDomainException.class)
            .extracting("baseCode")
            .isEqualTo(SurveyErrorCode.FORM_RESPONSE_LOOKUP_AMBIGUOUS);

        then(loadFormResponsePort).should(never())
            .findSubmittedByFormIdAndRespondentMemberId(anyLong(), anyLong());
        then(saveFormResponsePort).should(never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("draft 제출 scope가 있으면 전달된 required question만 필수 응답 검증한다")
    void draft_제출_scope가_있으면_전달된_required_question만_검증한다() {
        FormResponse draft = draftResponse();
        Question commonRequiredQuestion = question(10L, true);
        given(loadFormResponsePort.findById(FORM_RESPONSE_ID)).willReturn(Optional.of(draft));
        given(loadAnswerPort.listByFormResponseId(FORM_RESPONSE_ID))
            .willReturn(List.of(answer(draft, commonRequiredQuestion)));

        sut.submitDraft(SubmitDraftFormResponseCommand.builder()
            .formResponseId(FORM_RESPONSE_ID)
            .requesterMemberId(MEMBER_ID)
            .requiredQuestionIds(Set.of(commonRequiredQuestion.getId()))
            .allowedQuestionIds(Set.of(commonRequiredQuestion.getId()))
            .build());

        then(loadQuestionPort).should(never()).listByFormId(FORM_ID);
        then(saveFormResponsePort).should().save(draft);
    }

    @Test
    @DisplayName("draft 제출 scope가 없으면 기존처럼 form 전체 required question을 검증한다")
    void draft_제출_scope가_없으면_form_전체_required_question을_검증한다() {
        FormResponse draft = draftResponse();
        Question answeredRequiredQuestion = question(10L, true);
        Question missingRequiredQuestion = question(20L, true);
        given(loadFormResponsePort.findById(FORM_RESPONSE_ID)).willReturn(Optional.of(draft));
        given(loadAnswerPort.listByFormResponseId(FORM_RESPONSE_ID))
            .willReturn(List.of(answer(draft, answeredRequiredQuestion)));
        given(loadQuestionPort.listByFormId(FORM_ID))
            .willReturn(List.of(answeredRequiredQuestion, missingRequiredQuestion));

        assertThatThrownBy(() -> sut.submitDraft(SubmitDraftFormResponseCommand.builder()
            .formResponseId(FORM_RESPONSE_ID)
            .requesterMemberId(MEMBER_ID)
            .build()))
            .isInstanceOf(SurveyDomainException.class)
            .extracting("baseCode")
            .isEqualTo(SurveyErrorCode.REQUIRED_QUESTION_NOT_ANSWERED);
    }

    @Test
    @DisplayName("draft 제출 scope의 allowed question 밖에 저장된 답변이 있으면 실패한다")
    void draft_제출_scope의_allowed_question_밖에_저장된_답변이면_실패한다() {
        FormResponse draft = draftResponse();
        Question hiddenQuestion = question(20L, false);
        given(loadFormResponsePort.findById(FORM_RESPONSE_ID)).willReturn(Optional.of(draft));
        given(loadAnswerPort.listByFormResponseId(FORM_RESPONSE_ID))
            .willReturn(List.of(answer(draft, hiddenQuestion)));

        assertThatThrownBy(() -> sut.submitDraft(SubmitDraftFormResponseCommand.builder()
            .formResponseId(FORM_RESPONSE_ID)
            .requesterMemberId(MEMBER_ID)
            .requiredQuestionIds(Set.of())
            .allowedQuestionIds(Set.of(10L))
            .build()))
            .isInstanceOf(SurveyDomainException.class)
            .extracting("baseCode")
            .isEqualTo(SurveyErrorCode.QUESTION_IS_NOT_OWNED_BY_FORM);

        then(saveFormResponsePort).should(never()).save(any());
    }

    private Form publishedForm(boolean allowDuplicateResponses) {
        Form form = Form.createDraft("프로젝트 지원서", 1L, allowDuplicateResponses);
        ReflectionTestUtils.setField(form, "id", FORM_ID);
        form.publish();
        return form;
    }

    private FormResponse draftResponse() {
        FormResponse response = FormResponse.createDraft(publishedForm(true), MEMBER_ID);
        ReflectionTestUtils.setField(response, "id", FORM_RESPONSE_ID);
        return response;
    }

    private Question question(Long questionId, boolean isRequired) {
        Question question = Question.create("질문", QuestionType.SHORT_TEXT, isRequired, 1L);
        ReflectionTestUtils.setField(question, "id", questionId);
        return question;
    }

    private Answer answer(FormResponse formResponse, Question question) {
        return Answer.create(formResponse, question, QuestionType.SHORT_TEXT, "답변", null);
    }
}
