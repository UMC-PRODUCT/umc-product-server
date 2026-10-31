package com.umc.product.community.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import com.umc.product.community.application.port.in.command.report.ReportCommunityMemberUseCase;
import com.umc.product.community.application.port.in.command.report.dto.ReportCommunityMemberCommand;
import com.umc.product.community.application.port.in.query.report.dto.CommunityMemberReportReceiptInfo;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.report.domain.enums.ReportReason;

@WebMvcTest(controllers = CommunityMemberReportController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Community 사용자 신고 REST adapter")
class CommunityMemberReportControllerTest {

    private static final Long REQUESTER_ID = 99L;
    private static final Instant NOW = Instant.parse("2026-11-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ReportCommunityMemberUseCase reportMemberUseCase;

    @BeforeEach
    void setUpCurrentMember() {
        MemberPrincipal principal = new MemberPrincipal(REQUESTER_ID);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("사용자 신고는 CurrentMember와 사유, 상세 내용을 use case에 전달하고 접수 응답을 돌려준다")
    void reportMember_usesCurrentMemberAndReturnsReceipt() throws Exception {
        given(reportMemberUseCase.report(any())).willReturn(
            new CommunityMemberReportReceiptInfo(700L, 500L, ReportReason.HARASSMENT, NOW)
        );

        mockMvc.perform(post("/api/v1/community/members/500/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason":"HARASSMENT","detail":"반복해서 불쾌한 메시지를 보내요."}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.reportId").value("700"))
            .andExpect(jsonPath("$.result.memberId").value("500"))
            .andExpect(jsonPath("$.result.reason").value("HARASSMENT"))
            .andExpect(jsonPath("$.result.reporterId").doesNotExist());

        ArgumentCaptor<ReportCommunityMemberCommand> captor =
            ArgumentCaptor.forClass(ReportCommunityMemberCommand.class);
        then(reportMemberUseCase).should().report(captor.capture());
        assertThat(captor.getValue().targetMemberId()).isEqualTo(500L);
        assertThat(captor.getValue().requesterMemberId()).isEqualTo(REQUESTER_ID);
        assertThat(captor.getValue().reason()).isEqualTo(ReportReason.HARASSMENT);
        assertThat(captor.getValue().detail()).isEqualTo("반복해서 불쾌한 메시지를 보내요.");
    }

    @Test
    @DisplayName("상세 내용이 정확히 500자면 접수한다")
    void reportMember_acceptsDetailOfMaxLength() throws Exception {
        given(reportMemberUseCase.report(any())).willReturn(
            new CommunityMemberReportReceiptInfo(700L, 500L, ReportReason.SPAM, NOW)
        );

        mockMvc.perform(post("/api/v1/community/members/500/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"SPAM\",\"detail\":\"" + "가".repeat(500) + "\"}"))
            .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\\n\\t"})
    @DisplayName("상세 내용이 비었거나 공백뿐이면 400으로 거절한다")
    void reportMember_rejectsBlankDetail(String detail) throws Exception {
        mockMvc.perform(post("/api/v1/community/members/500/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"SPAM\",\"detail\":\"" + detail + "\"}"))
            .andExpect(status().isBadRequest());

        then(reportMemberUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상세 내용이 없으면 400으로 거절한다")
    void reportMember_rejectsMissingDetail() throws Exception {
        mockMvc.perform(post("/api/v1/community/members/500/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"SPAM\"}"))
            .andExpect(status().isBadRequest());

        then(reportMemberUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상세 내용이 501자면 400으로 거절한다")
    void reportMember_rejectsTooLongDetail() throws Exception {
        mockMvc.perform(post("/api/v1/community/members/500/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"SPAM\",\"detail\":\"" + "가".repeat(501) + "\"}"))
            .andExpect(status().isBadRequest());

        then(reportMemberUseCase).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOT_A_REASON", "ABUSE", "INAPPROPRIATE", "PRIVACY"})
    @DisplayName("없는 사유이거나 사용자 신고에 쓸 수 없는 사유면 400으로 거절한다")
    void reportMember_rejectsInvalidReason(String reason) throws Exception {
        mockMvc.perform(post("/api/v1/community/members/500/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"" + reason + "\",\"detail\":\"상세\"}"))
            .andExpect(status().isBadRequest());

        then(reportMemberUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("사유가 없으면 400으로 거절한다")
    void reportMember_rejectsMissingReason() throws Exception {
        mockMvc.perform(post("/api/v1/community/members/500/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"detail\":\"상세\"}"))
            .andExpect(status().isBadRequest());

        then(reportMemberUseCase).shouldHaveNoInteractions();
    }
}
