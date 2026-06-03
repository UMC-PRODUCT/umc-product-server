package com.umc.product.member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.authentication.domain.EmailVerificationPurpose;
import com.umc.product.member.adapter.out.persistence.MemberJpaRepository;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;
import com.umc.product.support.fixture.TermFixture;
import com.umc.product.term.adapter.out.persistence.TermConsentRepository;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.term.domain.enums.TermType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MemberCommandController 통합 테스트")
class MemberCommandControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private LoadGisuPort loadGisuPort;

    @Autowired
    private ChapterFixture chapterFixture;

    @Autowired
    private SchoolFixture schoolFixture;

    @Autowired
    private TermFixture termFixture;

    @Autowired
    private GetTermUseCase getTermUseCase;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private TermConsentRepository termConsentRepository;

    @Test
    @DisplayName("이메일 회원가입에 성공하면 회원, 자격증명, 필수 약관 동의가 저장된다")
    void 이메일_회원가입에_성공하면_회원_자격증명_필수_약관_동의가_저장된다() throws Exception {
        // given
        School school = activeSchool("회원가입학교", 9101L);
        List<Long> requiredTermIds = requiredTermIds();
        String email = "email-register-e2e@test.com";

        given(jwtTokenProvider.parseEmailVerificationToken("email-token", EmailVerificationPurpose.REGISTER))
            .willReturn(email);
        given(jwtTokenProvider.createAccessToken(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.isNull()))
            .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(org.mockito.ArgumentMatchers.anyLong()))
            .willReturn("refresh-token");

        // when & then
        mockMvc.perform(post("/api/v1/member/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emailRegisterRequest(school.getId(), requiredTermIds)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.memberId").isString())
            .andExpect(jsonPath("$.result.accessToken").value("access-token"))
            .andExpect(jsonPath("$.result.refreshToken").value("refresh-token"));

        Member savedMember = memberJpaRepository.findByEmail(email).orElseThrow();
        assertThat(savedMember.getName()).isEqualTo("홍길동");
        assertThat(savedMember.getNickname()).isEqualTo("길동");
        assertThat(savedMember.getSchoolId()).isEqualTo(school.getId());
        assertThat(savedMember.getPasswordHash())
            .isNotBlank()
            .isNotEqualTo("Password123!");
        assertThat(termConsentRepository.findByMemberId(savedMember.getId()))
            .extracting("termId")
            .containsExactlyInAnyOrderElementsOf(requiredTermIds);
    }

    private School activeSchool(String schoolName, Long generation) {
        Gisu gisu = loadGisuPort.findActiveGisu()
            .orElseGet(() -> gisuFixture.활성_기수(generation));
        Chapter chapter = chapterFixture.지부(gisu, schoolName + "지부");
        return schoolFixture.지부에_소속된_학교(schoolName, chapter);
    }

    private List<Long> requiredTermIds() {
        Set<Long> requiredTermIds = getTermUseCase.getRequiredTermIds();
        if (requiredTermIds.isEmpty()) {
            termFixture.필수_약관(TermType.SERVICE);
            termFixture.필수_약관(TermType.PRIVACY);
            requiredTermIds = getTermUseCase.getRequiredTermIds();
        }
        return requiredTermIds.stream().sorted().toList();
    }

    private String emailRegisterRequest(Long schoolId, List<Long> requiredTermIds) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("rawPassword", "Password123!");
        request.put("name", "홍길동");
        request.put("nickname", "길동");
        request.put("emailVerificationToken", "email-token");
        request.put("schoolId", schoolId);
        request.put("termsAgreements", requiredTermIds.stream()
            .map(termId -> {
                Map<String, Object> termAgreement = new LinkedHashMap<>();
                termAgreement.put("termsId", termId);
                termAgreement.put("isAgreed", true);
                return termAgreement;
            })
            .toList());
        return objectMapper.writeValueAsString(request);
    }
}
