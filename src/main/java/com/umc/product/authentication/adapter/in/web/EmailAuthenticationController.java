package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.CompleteEmailVerificationRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.SendEmailVerificationRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.CompleteEmailVerificationResponse;
import com.umc.product.authentication.adapter.in.web.dto.response.SendEmailVerificationResponse;
import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class EmailAuthenticationController {

    private final ManageAuthenticationUseCase manageAuthenticationUseCase;
    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final VerifyOAuthTokenPort verifyOAuthTokenPort;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "6자리 인증코드로 이메일 인증",
        description = """
            이메일로 발송된 인증코드를 통해서 이메일 인증을 완료합니다.

            emailVerificationToken을 발급하며, 해당 토큰을 회원가입 시에 제공해야 합니다.
            """)
    @PostMapping("email-verification/code")
    @Public
    public CompleteEmailVerificationResponse verifyEmailByCode(
        @RequestBody CompleteEmailVerificationRequest request
    ) {
        String emailVerificationToken = manageAuthenticationUseCase
            .validateEmailVerificationSession(
                ValidateEmailVerificationSessionCommand
                    .builder()
                    .sessionId(request.emailVerificationId().toString())
                    .code(request.verificationCode())
                    .build()
            );

        return CompleteEmailVerificationResponse
            .builder()
            .emailVerificationToken(emailVerificationToken)
            .build();
    }

    @Operation(summary = "이메일 인증 코드 발송",
        description = """
            인증을 요청하는 이메일로 인증 코드를 발송합니다.

            이메일 인증코드는 6자리의 숫자로만 구성되어 있습니다.
            """)
    @PostMapping("email-verification")
    @Public
    public SendEmailVerificationResponse sendEmailVerification(
        @RequestBody SendEmailVerificationRequest request
    ) {
        Long sessionId = manageAuthenticationUseCase.createEmailVerificationSession(request.email());

        return SendEmailVerificationResponse
            .builder()
            .emailVerificationId(sessionId.toString())
            .build();
    }
}
