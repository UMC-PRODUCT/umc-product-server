package com.umc.product.authentication.adapter.in.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.LogoutCommand;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("TokenAuthenticationController")
class TokenAuthenticationControllerTest extends IntegrationTestSupport {

    @MockitoBean
    ManageAuthenticationUseCase manageAuthenticationUseCase;

    @Test
    @DisplayName("로그아웃은 Authorization 헤더 없이 RefreshToken만으로 성공한다")
    void 로그아웃은_refresh_token만으로_성공() throws Exception {
        // when / then
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "refresh-token"
                    }
                    """))
            .andExpect(status().isOk());

        then(manageAuthenticationUseCase).should()
            .logout(argThat(command -> command.refreshToken().equals("refresh-token")));
    }

    @Test
    @DisplayName("로그아웃은 만료된 AccessToken이 Authorization 헤더에 있어도 RefreshToken만으로 성공한다")
    void 로그아웃은_만료된_access_token이_있어도_refresh_token만으로_성공() throws Exception {
        String expiredAccessToken = "expired-access-token";
        given(jwtTokenProvider.validateAccessToken(expiredAccessToken))
            .willThrow(new AuthenticationDomainException(AuthenticationErrorCode.EXPIRED_JWT_TOKEN));

        // when / then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + expiredAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "refresh-token"
                    }
                    """))
            .andExpect(status().isOk());

        then(manageAuthenticationUseCase).should()
            .logout(argThat(command -> command.refreshToken().equals("refresh-token")));
    }

    @Test
    @DisplayName("로그아웃 요청의 refreshToken이 blank이면 400을 반환한다")
    void 로그아웃_refresh_token_blank_거부() throws Exception {
        // when / then
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": " "
                    }
                    """))
            .andExpect(status().isBadRequest());

        then(manageAuthenticationUseCase).should(never()).logout(org.mockito.ArgumentMatchers.any(LogoutCommand.class));
    }
}
