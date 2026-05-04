package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.CreateQuestionCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteQuestionCommand;
import com.umc.product.survey.application.port.in.command.dto.ReorderQuestionsCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateQuestionCommand;

/**
 * Question(질문) 관리 UseCase.
 * <p>
 * 질문은 FormSection 에 속하며, 타입별로 하위 {@code QuestionOption}을 가질 수 있다 (RADIO/CHECKBOX/DROPDOWN).
 * 발행된 폼의 질문 구조 변경은 응답 일관성을 해치므로 DRAFT 상태에서만 허용. TODO: 관련 로직 확정 시 수정
 */
public interface ManageQuestionUseCase {

    /**
     * 섹션에 질문을 추가한다.
     * orderNo는 Service가 해당 섹션의 마지막 질문 뒤에 자동 부여.
     *
     * @return 생성된 Question ID
     */
    Long createQuestion(CreateQuestionCommand command);

    /**
     * 질문의 속성(title / description / type / isRequired)을 업데이트한다.
     * type 변경이 기존 선택지/응답과 불일치를 만드는 경우 Service가 정리 또는 예외 처리.
     * 발행된 폼의 질문은 수정 불가 — SURVEY_NOT_DRAFT 예외. TODO: 관련 로직 확정 시 수정
     */
    void updateQuestion(UpdateQuestionCommand command);

    /**
     * 질문 삭제. 연관 QuestionOption/AnswerChoice 도 cascade 삭제.
     */
    void deleteQuestion(DeleteQuestionCommand command);

    /**
     * 섹션 내 질문들의 순서를 재배치한다.
     * 입력 리스트 순서대로 orderNo가 1부터 재부여된다.
     * 섹션에 속한 모든 질문 ID가 포함되어야 한다.
     */
    void reorderQuestions(ReorderQuestionsCommand command);
}
