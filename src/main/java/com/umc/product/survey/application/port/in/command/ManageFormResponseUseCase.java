package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.*;

/**
 * FormResponse(폼 응답) 관리 UseCase.
 * <p>
 * 두 가지 응답 플로우를 지원한다:
 * <ol>
 *   <li><b>즉시 제출</b> (vote 등) — {@link #submitImmediately} 한 번 호출. draft 없음.
 *       이후 {@link #updateResponse} / {@link #deleteResponse} 로 수정·취소 가능.</li>
 *   <li><b>draft 플로우</b> (지원서 등) — {@link #createDraft} 로 시작,
 *       {@link #updateDraft} 로 임시저장 반복, {@link #submitDraft} 로 최종 제출.
 *       최종 제출 전에 {@link #deleteDraft} 로 포기 가능.</li>
 * </ol>
 * 제출 후에는 두 플로우 모두 {@link #updateResponse} / {@link #deleteResponse} 로 관리된다.
 */
public interface ManageFormResponseUseCase {

    /**
     * 폼에 대한 응답을 즉시 제출한다. (draft 없이 바로 SUBMITTED 상태 생성)
     * vote 같이 한 번에 제출하는 플로우에서 사용.
     * 같은 폼에 이미 제출한 응답이 있으면 예외.
     *
     * @return 생성된 FormResponse ID
     */
    Long submitImmediately(SubmitFormResponseCommand command);

    /**
     * 기존 SUBMITTED 응답의 답변을 전체 교체한다.
     * 해당 폼에 대한 기존 SUBMITTED 응답이 없으면 예외.
     */
    void updateResponse(UpdateFormResponseCommand command);

    /**
     * 본인이 제출한 SUBMITTED 응답을 삭제한다. (FormResponse + 연관 Answer 모두 삭제)
     * 삭제 후 다시 제출 가능. 기존 응답이 없으면 예외.
     * DRAFT 상태 응답 삭제는 {@link #deleteDraft} 사용.
     */
    void deleteResponse(DeleteFormResponseCommand command);

    /**
     * 폼에 대한 draft 응답을 최초 생성한다. (빈 draft).
     * 이후 {@link #updateDraft} 로 답변을 채워나가고 {@link #submitDraft} 로 최종 제출.
     * 같은 폼에 이미 draft 또는 SUBMITTED 응답이 있으면 예외.
     *
     * @return 생성된 FormResponse ID
     */
    Long createDraft(CreateDraftFormResponseCommand command);

    /**
     * 기존 draft 응답의 답변을 전체 교체한다. (임시저장)
     * draft가 아닌 응답(SUBMITTED) 또는 존재하지 않는 응답 ID면 예외.
     */
    void updateDraft(UpdateDraftFormResponseCommand command);

    /**
     * draft 응답을 SUBMITTED 로 전환(최종 제출)한다.
     * draft가 아닌 응답이면 예외. 답변 내용은 이전 updateDraft 값 그대로 유지.
     */
    void submitDraft(SubmitDraftFormResponseCommand command);

    /**
     * draft 응답을 삭제한다. (연관 Answer 포함)
     * SUBMITTED 상태인 응답을 이 메서드로 삭제하면 예외 — SUBMITTED 삭제는 {@link #deleteResponse} 사용.
     */
    void deleteDraft(DeleteDraftFormResponseCommand command);
}
