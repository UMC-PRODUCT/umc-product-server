package com.umc.product.project.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.SubmitUserFeedbackResponseRequest;
import com.umc.product.project.adapter.in.web.dto.response.GetUserFeedbackTemplateResponse;
import com.umc.product.project.adapter.in.web.dto.response.UserFeedbackSubmitResponse;
import com.umc.product.project.application.port.in.command.SubmitUserFeedbackResponseUseCase;
import com.umc.product.project.application.port.in.query.GetUserFeedbackTemplateUseCase;
import com.umc.product.project.domain.enums.UserFeedbackContext;

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
            요청자의 챌린저 이력 및 운영진 역할을 기반으로 TargetType을 자동 판별하여,
            해당 context에 맞는 활성 피드백 템플릿(Survey 폼 전체 구조 포함)을 반환합니다.
            활성 기수가 없거나 해당 context + targetType 조합의 템플릿이 없으면 result = null.
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
            지정한 피드백 템플릿에 대한 응답을 즉시 제출합니다.
            동일 폼에 이미 응답한 경우 Survey 도메인에서 중복 응답 예외가 발생합니다.
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
