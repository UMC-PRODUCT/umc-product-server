package com.umc.product.certificate.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.certificate.application.port.in.command.AdminIssueCertificateUseCase;
import com.umc.product.certificate.application.port.in.command.IssueCertificateUseCase;
import com.umc.product.certificate.application.port.in.command.RevokeCertificateUseCase;
import com.umc.product.certificate.application.port.in.command.dto.CertificateIssueInfo;
import com.umc.product.certificate.application.port.in.command.dto.RevokeCertificateCommand;
import com.umc.product.certificate.application.port.in.query.GetCertificateUseCase;
import com.umc.product.certificate.application.port.in.query.dto.CertificateVerificationInfo;
import com.umc.product.certificate.domain.CertificateStatus;
import com.umc.product.certificate.domain.CertificateType;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;

@WebMvcTest(controllers = {CertificateController.class, AdminCertificateController.class})
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CertificateController")
class CertificateControllerTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2027-07-01T00:00:00Z");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    IssueCertificateUseCase issueCertificateUseCase;

    @MockitoBean
    AdminIssueCertificateUseCase adminIssueCertificateUseCase;

    @MockitoBean
    RevokeCertificateUseCase revokeCertificateUseCase;

    @MockitoBean
    GetCertificateUseCase getCertificateUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(99L)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("본인 인증서를 셀프 발급한다")
    void 본인_인증서를_셀프_발급한다() throws Exception {
        // given
        given(issueCertificateUseCase.issue(any())).willReturn(issueInfo("UMC-CMP-20260701-ABCDEFGH"));

        // when & then
        mockMvc.perform(post("/api/v1/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new SelfIssueRequestBody(
                    CertificateType.COMPLETION,
                    7L,
                    null
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.serialNumber").value("UMC-CMP-20260701-ABCDEFGH"))
            .andExpect(jsonPath("$.result.type").value("COMPLETION"));
    }

    @Test
    @DisplayName("공개 검증 API는 유효 여부와 마스킹된 이름을 반환한다")
    void 공개_검증_API는_유효_여부와_마스킹된_이름을_반환한다() throws Exception {
        // given
        given(getCertificateUseCase.verifyBySerialNumber("UMC-CMP-20260701-ABCDEFGH"))
            .willReturn(new CertificateVerificationInfo(
                true,
                "ISSUED",
                CertificateType.COMPLETION,
                "김*엠",
                ISSUED_AT,
                EXPIRES_AT
            ));

        // when & then
        mockMvc.perform(get("/api/v1/certificates/verify/UMC-CMP-20260701-ABCDEFGH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.valid").value(true))
            .andExpect(jsonPath("$.result.status").value("ISSUED"))
            .andExpect(jsonPath("$.result.recipientName").value("김*엠"));
    }

    @Test
    @DisplayName("운영진 인증서 폐기 요청을 command로 변환한다")
    void 운영진_인증서_폐기_요청을_command로_변환한다() throws Exception {
        // when
        mockMvc.perform(patch("/api/v1/admin/certificates/{certificateId}/revoke", 10L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new RevokeRequestBody("오발급"))))
            .andExpect(status().isOk());

        // then
        verify(revokeCertificateUseCase).revoke(RevokeCertificateCommand.builder()
            .certificateId(10L)
            .requesterMemberId(99L)
            .reason("오발급")
            .build());
    }

    private CertificateIssueInfo issueInfo(String serialNumber) {
        return new CertificateIssueInfo(
            1L,
            serialNumber,
            CertificateType.COMPLETION,
            CertificateStatus.ISSUED,
            ISSUED_AT,
            EXPIRES_AT
        );
    }

    private record SelfIssueRequestBody(
        CertificateType type,
        Long gisuId,
        Long projectId
    ) {
    }

    private record RevokeRequestBody(String reason) {
    }
}
