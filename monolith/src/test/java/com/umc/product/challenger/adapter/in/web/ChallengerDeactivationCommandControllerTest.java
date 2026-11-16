package com.umc.product.challenger.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;

@WebMvcTest(controllers = ChallengerDeactivationCommandController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerDeactivationCommandController")
class ChallengerDeactivationCommandControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    RegisterChallengerDeactivationUseCase registerChallengerDeactivationUseCase;

    @BeforeEach
    void setUp() {
        MemberPrincipal principal = new MemberPrincipal(10L);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("현재 로그인한 운영진으로 탈부를 등록한다")
    void registerDeactivation() throws Exception {
        given(registerChallengerDeactivationUseCase.register(any())).willReturn(500L);

        mockMvc.perform(post("/api/v1/challenger/admin/{challengerId}/deactivations", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "VOLUNTARY_OTHER",
                      "reasonDetail": "해외 교환학생 파견",
                      "memo": "본인 의사 확인",
                      "imageIds": ["image-1"],
                      "links": ["https://example.com/evidence"]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.deactivationId").value(500L));

        ArgumentCaptor<RegisterChallengerDeactivationCommand> captor =
            ArgumentCaptor.forClass(RegisterChallengerDeactivationCommand.class);
        then(registerChallengerDeactivationUseCase).should().register(captor.capture());
        assertThat(captor.getValue().challengerId()).isEqualTo(100L);
        assertThat(captor.getValue().registeredByMemberId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("사진이 5장을 초과하면 400을 반환한다")
    void rejectTooManyImages() throws Exception {
        mockMvc.perform(post("/api/v1/challenger/admin/{challengerId}/deactivations", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "VOLUNTARY_PERSONAL_CIRCUMSTANCES",
                      "imageIds": ["1", "2", "3", "4", "5", "6"]
                    }
                    """))
            .andExpect(status().isBadRequest());

        then(registerChallengerDeactivationUseCase).should(never())
            .register(any(RegisterChallengerDeactivationCommand.class));
    }

    @Test
    @DisplayName("링크가 3개를 초과하면 400을 반환한다")
    void rejectTooManyLinks() throws Exception {
        mockMvc.perform(post("/api/v1/challenger/admin/{challengerId}/deactivations", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reason": "VOLUNTARY_PERSONAL_CIRCUMSTANCES",
                      "links": [
                        "https://example.com/1",
                        "https://example.com/2",
                        "https://example.com/3",
                        "https://example.com/4"
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest());

        then(registerChallengerDeactivationUseCase).should(never())
            .register(any(RegisterChallengerDeactivationCommand.class));
    }

    @Test
    @DisplayName("탈부 사유가 없으면 400을 반환한다")
    void rejectMissingReason() throws Exception {
        mockMvc.perform(post("/api/v1/challenger/admin/{challengerId}/deactivations", 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "memo": "운영진 메모"
                    }
                    """))
            .andExpect(status().isBadRequest());

        then(registerChallengerDeactivationUseCase).should(never())
            .register(any(RegisterChallengerDeactivationCommand.class));
    }
}
