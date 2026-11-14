package com.umc.product.global.security.resolver;

import static com.umc.product.global.security.JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.global.security.CurrentMemberProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

@DisplayName("CurrentMemberArgumentResolver")
class CurrentMemberArgumentResolverTest {

    private final CurrentMemberArgumentResolver sut = new CurrentMemberArgumentResolver(new CurrentMemberProvider());
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("로그인한 회원이면 MemberPrincipal을 넘긴다")
    void 로그인한_회원이면_MemberPrincipal을_넘긴다() throws Exception {
        // Given
        MemberPrincipal member = new MemberPrincipal(10L);
        authenticate(member);

        // When
        Object result = resolve(requiredParameter());

        // Then
        assertThat(result).isSameAs(member);
    }

    @Test
    @DisplayName("로그인하지 않은 요청이 필수 파라미터를 요구하면 인증 예외를 던진다")
    void 익명_요청은_인증_예외를_던진다() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        // When & Then
        assertThatThrownBy(() -> resolve(requiredParameter()))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @DisplayName("로그인하지 않은 요청도 required = false 파라미터에는 null을 넘긴다")
    void 익명_요청은_선택_파라미터에_null을_넘긴다() throws Exception {
        // When
        Object result = resolve(optionalParameter());

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("회원이 아닌 인증이 필수 파라미터를 요구하면 권한 예외를 던진다")
    void 회원이_아닌_인증은_권한_예외를_던진다() {
        // Given
        authenticate("demoday-guest");

        // When & Then
        assertThatThrownBy(() -> resolve(requiredParameter()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("회원이 아닌 인증이어도 회원 토큰이 실패했다면 인증 예외를 던진다")
    void 토큰이_실패한_게스트_요청은_인증_예외를_던진다() {
        // Given
        authenticate("demoday-guest");
        request.setAttribute(JWT_ERROR_ATTRIBUTE,
            new AuthenticationDomainException(AuthenticationErrorCode.EXPIRED_JWT_TOKEN));

        // When & Then
        assertThatThrownBy(() -> resolve(requiredParameter()))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private void authenticate(Object principal) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private Object resolve(MethodParameter parameter) throws Exception {
        return sut.resolveArgument(parameter, null, new ServletWebRequest(request), null);
    }

    private MethodParameter requiredParameter() throws NoSuchMethodException {
        return parameterOf("required");
    }

    private MethodParameter optionalParameter() throws NoSuchMethodException {
        return parameterOf("optional");
    }

    private MethodParameter parameterOf(String methodName) throws NoSuchMethodException {
        Method method = Handlers.class.getDeclaredMethod(methodName, MemberPrincipal.class);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private static class Handlers {

        void required(@CurrentMember MemberPrincipal memberPrincipal) {
        }

        void optional(@CurrentMember(required = false) MemberPrincipal memberPrincipal) {
        }
    }
}
