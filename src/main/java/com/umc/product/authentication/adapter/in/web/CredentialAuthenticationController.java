package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.ChangePasswordRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.LoginByIdPwRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.RegisterCredentialRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.IdPwLoginResponse;
import com.umc.product.authentication.adapter.in.web.dto.response.LoginIdAvailabilityResponse;
import com.umc.product.authentication.application.port.in.command.CredentialAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.IdPwLoginResult;
import com.umc.product.authentication.application.port.in.query.CheckCredentialAvailabilityUseCase;
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

    @Public
    @GetMapping("/login-id/availability")
    @Operation(summary = "로그인 ID 사용 가능 여부 조회",
        description = "loginId 가 형식에 맞고 아직 사용되지 않았는지 확인합니다. 형식이 잘못되면 400 응답입니다.")
    public LoginIdAvailabilityResponse checkLoginIdAvailability(
        @RequestParam String loginId
    ) {
        boolean available = checkCredentialAvailabilityUseCase.isLoginIdAvailable(loginId);
        return LoginIdAvailabilityResponse.of(loginId, available);
    }

    @PostMapping("/credentials")
    @Operation(summary = "ID/PW 자격증명 최초 등록",
        description = "OAuth 로 가입한 회원이 ID/PW 자격증명을 추가하거나 자격증명만으로 가입한 회원이 사용합니다.")
    public void registerCredential(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody RegisterCredentialRequest request
    ) {
        credentialAuthenticationUseCase.registerCredential(
            request.toCommand(memberPrincipal.getMemberId())
        );
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경",
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
    @PostMapping("/login/id-pw")
    @Operation(summary = "ID/PW 로그인",
        description = "loginId/password 로 인증하여 AccessToken/RefreshToken 을 발급받습니다.")
    public IdPwLoginResponse loginByIdPw(
        @Valid @RequestBody LoginByIdPwRequest request
    ) {
        IdPwLoginResult result = credentialAuthenticationUseCase.loginByIdPw(request.toCommand());
        return IdPwLoginResponse.from(result);
    }
}
