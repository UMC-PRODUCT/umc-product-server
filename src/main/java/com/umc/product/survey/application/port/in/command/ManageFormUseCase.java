package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormCommand;
import com.umc.product.survey.application.port.in.command.dto.PublishFormCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormCommand;

/**
 * Form(폼) 생명주기 관리 UseCase.
 * <p>
 * 생성 흐름: {@link #createDraft} 로 DRAFT 상태로 시작 -> {@link #updateForm}으로 임시저장 업데이트 -> {@link #publishForm} 로 PUBLISHED 전환 시 응답 수집 가능.
 * <p>
 * 삭제는 {@link #deleteForm} — 연관된 FormSection / Question / QuestionOption / FormResponse / Answer 모두 cascade 삭제.
 */
public interface ManageFormUseCase {

    /**
     * 폼을 DRAFT 상태로 최초 생성한다. 이 단계에선 응답을 받을 수 없다.
     *
     * @return 생성된 Form ID
     */
    Long createDraft(CreateDraftFormCommand command);

    /**
     * DRAFT 상태의 폼 메타데이터(title/description/isAnonymous)를 업데이트한다. (임시저장 용도)
     * 발행된 폼은 수정 불가 — SURVEY_ALREADY_PUBLISHED 예외. TODO: 관련 로직 확정 시 수정
     * 요청자가 작성자가 아니면 권한 예외.
     */
    void updateForm(UpdateFormCommand command);

    /**
     * 폼을 DRAFT -> PUBLISHED 전환한다. 이후부터 응답 수집 가능.
     * 이미 PUBLISHED인 폼을 다시 발행하면 SURVEY_ALREADY_PUBLISHED 예외.
     */
    void publishForm(PublishFormCommand command);

    /**
     * 폼과 연관 구조(섹션/질문/옵션/응답/답변) 전부 삭제.
     * 요청자가 작성자가 아니면 권한 예외.
     */
    void deleteForm(DeleteFormCommand command);
}
