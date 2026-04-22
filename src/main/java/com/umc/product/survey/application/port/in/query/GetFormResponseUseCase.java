package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.application.port.in.query.dto.FormResponseInfo;
import java.util.List;
import java.util.Optional;

/**
 * FormResponse 조회 UseCase.
 * <p>
 * 폼 응답을 다양한 기준으로 조회한다. DRAFT / SUBMITTED 상태 구분해 조회할 수 있도록
 * 상태별 조회 메서드를 제공.
 */
public interface GetFormResponseUseCase {

    /**
     * 응답 ID 로 단건 조회. 없으면 Optional.empty.
     */
    Optional<FormResponseInfo> findById(Long formResponseId);

    /**
     * 응답 ID 로 단건 조회. 없으면 FORM_RESPONSE_NOT_FOUND 예외.
     */
    FormResponseInfo getById(Long formResponseId);

    /**
     * 폼에 대한 모든 응답을 반환한다. (DRAFT + SUBMITTED 모두)
     * 폼 작성자 대시보드 용도.
     */
    List<FormResponseInfo> getAllByFormId(Long formId);

    /**
     * 특정 사용자의 모든 draft 응답을 반환한다. "내가 작성 중인 응답 목록" 용도.
     */
    List<FormResponseInfo> getAllDraftByRespondentMemberId(Long respondentMemberId);

    /**
     * 특정 폼에 대한 특정 사용자의 draft 응답을 조회. 없으면 Optional.empty.
     * "작성 중 응답 이어서 보기" 용도.
     */
    Optional<FormResponseInfo> findDraftByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId);

    /**
     * 특정 폼에 대한 특정 사용자의 SUBMITTED 응답을 조회. 없으면 Optional.empty.
     */
    Optional<FormResponseInfo> findSubmittedByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId);
}
