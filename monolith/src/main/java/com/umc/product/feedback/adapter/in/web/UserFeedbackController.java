package com.umc.product.feedback.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.feedback.adapter.in.web.dto.request.SubmitUserFeedbackResponseRequest;
import com.umc.product.feedback.adapter.in.web.dto.response.GetUserFeedbackTemplateResponse;
import com.umc.product.feedback.adapter.in.web.dto.response.UserFeedbackSubmitResponse;
import com.umc.product.feedback.application.port.in.command.SubmitUserFeedbackResponseUseCase;
import com.umc.product.feedback.application.port.in.query.GetUserFeedbackTemplateUseCase;
import com.umc.product.feedback.domain.enums.UserFeedbackContext;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user-feedbacks")
@RequiredArgsConstructor
@Tag(name = "UserFeedback | 사용자 조사", description = "사용자 피드백 템플릿 조회 및 응답 제출")
public class UserFeedbackController {

    private final GetUserFeedbackTemplateUseCase getUserFeedbackTemplateUseCase;
    private final SubmitUserFeedbackResponseUseCase submitUserFeedbackResponseUseCase;

    @GetMapping("/templates")
    @Operation(
        summary = "사용자 피드백 템플릿 조회",
        description = """
            로그인 사용자에게 제공할 만족도 설문을 조회합니다.

            `context`로 조회할 설문을 지정합니다. 응답에는 템플릿 ID와 설문의 섹션·질문·선택지가 포함됩니다.

            ### 설문 구분: context

            - `APPLICATION_SUBMITTED`: 프로젝트 지원과정 설문
            - `MATCHING_COMPLETED`: 팀 매칭과정 설문
            - `APPLICATION_MONITORING`: 운영진 지원 현황 설문

            ### 설문을 반환하지 않는 경우

            아래 조건에서는 서버가 `result = null`을 반환하며, 실제 JSON 응답에는 **result 필드가 포함되지 않습니다.**

            - 해당 설문에 이미 응답한 경우
            - 활성 기수가 없는 경우
            - 사용자가 설문 대상에 해당하지 않는 경우
            - 요청한 종류에 제공할 설문이 없는 경우

            ### 질문 표시

            - 질문과 선택지는 `orderNo` 오름차순으로 반환합니다.
            - 운영진 설문(`ADMIN` / `APPLICATION_MONITORING`)의 조건부 질문도 응답에 포함됩니다.
            - 조건부 질문의 표시 여부는 프론트엔드에서 처리합니다.

            예: '필요한 정보를 찾는 데 어려움이 있었나요?'에 '조금 있었어요' 또는 '많이 있었어요'를 선택하면 추가 서술란을 표시합니다.
            """
    )
    public GetUserFeedbackTemplateResponse getTemplate(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam UserFeedbackContext context
    ) {
        return getUserFeedbackTemplateUseCase
            .findTemplate(memberPrincipal.getMemberId(), context)
            .map(GetUserFeedbackTemplateResponse::from)
            .orElse(null);
    }

    @PostMapping("/responses")
    @Operation(
        summary = "사용자 피드백 응답 제출",
        description = """
            지정한 만족도 설문에 로그인 사용자의 답변을 제출합니다.

            ### 요청 및 응답

            - 조회 API에서 받은 `templateId`와 질문·선택지 ID를 사용합니다.
            - 제출에 성공하면 저장된 응답의 `formResponseId`를 반환합니다.

            ### 중복 제출

            해당 설문에 이미 응답한 경우 HTTP 400과 `FORM-0027` 오류를 반환합니다.
            """
    )
    public UserFeedbackSubmitResponse submit(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody SubmitUserFeedbackResponseRequest request
    ) {
        Long formResponseId = submitUserFeedbackResponseUseCase.submit(
            request.toCommand(memberPrincipal.getMemberId())
        );
        return UserFeedbackSubmitResponse.from(formResponseId);
    }
}
