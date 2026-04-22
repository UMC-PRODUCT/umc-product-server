package com.umc.product.survey.application.port.in.command;

import com.umc.product.survey.application.port.in.command.dto.CancelFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.SubmitFormResponseCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormResponseCommand;

public interface ManageFormResponseUseCase {

    /**
     * 폼에 대한 새로운 응답을 제출합니다. (SUBMITTED 상태 FormResponse 생성)
     * 같은 폼에 이미 제출한 응답이 있으면 예외.
     *
     * @return 생성된 FormResponse의 ID
     */
    Long submit(SubmitFormResponseCommand command);

    /**
     * 기존 SUBMITTED 응답의 답변을 전체 교체합니다.
     * 해당 폼에 대한 기존 SUBMITTED 응답이 없으면 예외.
     */
    void updateResponse(UpdateFormResponseCommand command);

    /**
     * 본인이 제출한 SUBMITTED 응답을 취소합니다. (FormResponse + 연관 Answer 모두 삭제)
     * 취소 후 다시 submit 호출 시 정상적으로 재응답 가능.
     * 해당 폼에 대한 기존 SUBMITTED 응답이 없으면 예외.
     */
    void cancelResponse(CancelFormResponseCommand command);
}
