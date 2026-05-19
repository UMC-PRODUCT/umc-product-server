package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.ChangePasswordRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.LoginByEmailRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.RegisterCredentialRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.ResetPasswordByEmailRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.EmailAvailabilityResponse;
import com.umc.product.authentication.adapter.in.web.dto.response.LocalLoginResponse;
import com.umc.product.authentication.application.port.in.command.CredentialAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.LocalLoginResult;
import com.umc.product.authentication.application.port.in.query.CheckCredentialAvailabilityUseCase;
import com.umc.product.authentication.domain.EmailVerificationPurpose;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ID/PW 자격증명 등록/변경/조회/로그인 컨트롤러.
 * <p>
 * OAuth 와는 독립적인 자격증명 흐름을 담당하며, 응답 래핑은 GlobalResponseWrapper 가 수행한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication | 로그인", description = "ID/PW 최초 등록, 변경, ID 중복확인, OAuth 로그인, AT 갱신 등")
public class CredentialAuthenticationController {

    private final CredentialAuthenticationUseCase credentialAuthenticationUseCase;
    private final CheckCredentialAvailabilityUseCase checkCredentialAvailabilityUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/credentials")
    @Operation(summary = "[CREDENTIAL-002] 비밀번호 자격증명 최초 등록",
        description = "OAuth 로 가입한 회원이 이메일/비밀번호로 로그인할 수 있도록 비밀번호를 추가 등록합니다. "
            + "이메일은 회원의 기존 Member.email 을 그대로 사용합니다.")
    public void registerCredential(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody RegisterCredentialRequest request
    ) {
        credentialAuthenticationUseCase.registerCredentialByEmail(
            request.toCommand(memberPrincipal.getMemberId())
        );
    }

    @PatchMapping("/password")
    @Operation(summary = "[CREDENTIAL-003] 비밀번호 변경",
        description = "현재 비밀번호 검증 후 새 비밀번호로 교체합니다.")
    public void changePassword(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        credentialAuthenticationUseCase.changePassword(
            request.toCommand(memberPrincipal.getMemberId())
        );
    }

    @Public
    @PatchMapping("/password/reset")
    @Operation(summary = "[CREDENTIAL-007] 비밀번호 초기화",
        description = "비밀번호를 잊은 회원이 이메일 인증으로 발급받은 emailVerificationToken 을 사용해 "
            + "현재 비밀번호 없이 새 비밀번호로 교체합니다. 사용자 열거 방지를 위해 회원 없음/자격증명 미등록 등의 "
            + "사유는 모두 동일한 메시지로 응답합니다.")
    public void resetPasswordByEmail(
        @Valid @RequestBody ResetPasswordByEmailRequest request
    ) {
        String email = jwtTokenProvider.parseEmailVerificationToken(
            request.emailVerificationToken(),
            EmailVerificationPurpose.PASSWORD_RESET
        );
        credentialAuthenticationUseCase.resetPasswordByEmail(request.toCommand(email));
    }

    @Public
    @GetMapping("/email/availability")
    @Operation(summary = "[CREDENTIAL-005] 이메일 사용 가능 여부 조회",
        description = "email 형식이 유효하고 아직 사용되지 않았는지 확인합니다. 형식이 잘못되면 400 응답입니다.")
    public EmailAvailabilityResponse checkEmailAvailability(
        @RequestParam String email
    ) {
        boolean available = checkCredentialAvailabilityUseCase.isEmailAvailable(email);
        return EmailAvailabilityResponse.of(email, available);
    }

    @Public
    @PostMapping("/login/email")
    @Operation(summary = "[LOGIN-006] 이메일/PW 로그인",
        description = "email/password 로 인증하여 AccessToken/RefreshToken 을 발급받습니다. "
            + "clientType(ANDROID, IOS, WEB)을 함께 전달하면 AccessToken claim 으로 반영됩니다.")
    public LocalLoginResponse loginByEmail(
        @Valid @RequestBody LoginByEmailRequest request
    ) {
        LocalLoginResult result = credentialAuthenticationUseCase.loginByEmail(request.toCommand());
        return LocalLoginResponse.from(result);
    }
}
