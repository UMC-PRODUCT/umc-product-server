package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.CreateQuestionOptionCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteQuestionOptionCommand;
import com.umc.product.survey.application.port.in.command.dto.ReorderQuestionOptionsCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateQuestionOptionCommand;

/**
 * QuestionOption(질문 선택지) 관리 UseCase.
 * <p>
 * 선택지는 RADIO / CHECKBOX / DROPDOWN 타입 질문에만 유의미하며, 다른 타입 질문에는 생성하지 않는다.
 * 발행된 폼의 선택지 변경은 응답 무결성에 영향을 주므로 DRAFT 상태에서만 허용. TODO: 관련 로직 확정 시 수정
 */
public interface ManageQuestionOptionUseCase {

    /**
     * 질문에 선택지를 추가한다.
     * orderNo는 Service가 해당 질문의 마지막 선택지 뒤에 자동 부여.
     *
     * @return 생성된 QuestionOption ID
     */
    Long createOption(CreateQuestionOptionCommand command);

    /**
     * 선택지의 content / isOther 속성을 업데이트한다.
     * 발행된 폼의 선택지는 수정 불가 — SURVEY_NOT_DRAFT 예외. TODO: 관련 로직 확정 시 수정
     */
    void updateOption(UpdateQuestionOptionCommand command);

    /**
     * 선택지 삭제. 연관 AnswerChoice 의 question_option_id는 ON DELETE SET NULL 로 처리됨.
     */
    void deleteOption(DeleteQuestionOptionCommand command);

    /**
     * 질문 내 선택지들의 순서를 재배치한다.
     * {@code orderedOptionIds} 의 순서대로 orderNo가 1부터 재부여.
     */
    void reorderOptions(ReorderQuestionOptionsCommand command);
}
