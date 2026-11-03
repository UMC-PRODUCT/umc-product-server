package com.umc.product.member.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@AutoConfigureMockMvc(addFilters = false)
@DisplayName("명함 조회·수정 통합 테스트")
class MemberBusinessCardIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private ChapterFixture chapterFixture;

    @Autowired
    private SchoolFixture schoolFixture;

    @Autowired
    private ChallengerFixture challengerFixture;

    @Autowired
    private SaveMemberPort saveMemberPort;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("명함 수정 API로 비공개 저장한 항목은 내 명함과 타인 조회에서 제외되고 수정 화면 조회에는 남는다")
    void 명함_수정_API로_비공개_저장한_항목은_내_명함과_타인_조회에서_제외되고_수정_화면_조회에는_남는다() throws Exception {
        // given
        // 현재 활성 기수가 아닌 지난 기수 기록도 최신이면 명함에 표시한다.
        Gisu gisu = gisuFixture.비활성_기수(9401L);
        Chapter chapter = chapterFixture.지부(gisu, "명함지부");
        School school = schoolFixture.지부에_소속된_학교("명함학교", chapter);
        Member owner = saveMemberPort.save(Member.create("홍길동", "길동", "card-owner@test.com", school.getId(), null));
        Member viewer = saveMemberPort.save(Member.create("김철수", "철수", "card-viewer@test.com", school.getId(), null));
        challengerFixture.챌린저(owner.getId(), ChallengerPart.SPRINGBOOT, gisu.getId());
        challengerFixture.챌린저(viewer.getId(), ChallengerPart.WEB, gisu.getId());

        // when: 명함 수정 (연락처·GitHub 비공개, BLOG 공개)
        authenticate(owner.getId());
        mockMvc.perform(patch("/api/v1/member/me/business-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "contact": {"email": "gildong.contact@example.com", "isPublic": false},
                      "links": [
                        {"type": "GITHUB", "link": "https://github.com/gildong", "isPublic": false},
                        {"type": "BLOG", "link": "https://blog.example.com", "isPublic": true}
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.contact.email").value("gildong.contact@example.com"));

        // then: 수정 화면은 비공개 항목까지 5개 링크 모두 공개 여부와 함께 내려주고, 활동 이력은 최신 기수·파트다
        mockMvc.perform(get("/api/v1/member/me/business-card/edit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.name").value("홍길동"))
            .andExpect(jsonPath("$.result.schoolName").value("명함학교"))
            .andExpect(jsonPath("$.result.activity.generation").value(9401))
            .andExpect(jsonPath("$.result.activity.part").value("SPRINGBOOT"))
            .andExpect(jsonPath("$.result.oauthProviders").isEmpty())
            .andExpect(jsonPath("$.result.contact.email").value("gildong.contact@example.com"))
            .andExpect(jsonPath("$.result.contact.isPublic").value(false))
            .andExpect(jsonPath("$.result.links.length()").value(5))
            .andExpect(jsonPath("$.result.links[?(@.type == 'GITHUB')].isPublic").value(false))
            .andExpect(jsonPath("$.result.links[?(@.type == 'BLOG')].isPublic").value(true));

        // then: 내 명함은 공개 항목만 보여준다
        mockMvc.perform(get("/api/v1/member/me/business-card"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.activity.generation").value(9401))
            .andExpect(jsonPath("$.result.contact").doesNotExist())
            .andExpect(jsonPath("$.result.links.length()").value(1))
            .andExpect(jsonPath("$.result.links[0].type").value("BLOG"));

        // then: 기존 me 응답 형태는 그대로다
        mockMvc.perform(get("/api/v1/member/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.email").value("card-owner@test.com"))
            .andExpect(jsonPath("$.result.profile.github").value("https://github.com/gildong"))
            .andExpect(jsonPath("$.result.profile.contact").doesNotExist());

        // then: 다른 회원의 조회(MEMBER-101)에는 비공개 링크가 노출되지 않는다
        authenticate(viewer.getId());
        mockMvc.perform(get("/api/v1/member/profile/{memberId}", owner.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.email").doesNotExist())
            .andExpect(jsonPath("$.result.profile.github").doesNotExist())
            .andExpect(jsonPath("$.result.profile.blog").value("https://blog.example.com"));
    }

    @Test
    @DisplayName("기존 프로필 링크 수정 API로 링크를 저장해도 명함 공개 설정과 연락처는 유지된다")
    void 기존_프로필_링크_수정_API로_링크를_저장해도_명함_공개_설정과_연락처는_유지된다() throws Exception {
        // given
        Gisu gisu = gisuFixture.비활성_기수(9402L);
        Chapter chapter = chapterFixture.지부(gisu, "호환지부");
        School school = schoolFixture.지부에_소속된_학교("호환학교", chapter);
        Member owner = saveMemberPort.save(Member.create("홍길동", "길동", "card-legacy@test.com", school.getId(), null));
        authenticate(owner.getId());
        mockMvc.perform(patch("/api/v1/member/me/business-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "contact": {"email": "contact@example.com", "isPublic": true},
                      "links": [{"type": "GITHUB", "link": "https://github.com/old", "isPublic": false}]
                    }
                    """))
            .andExpect(status().isOk());

        // when: 구버전 앱·macOS 설정이 기존 API로 링크를 저장
        mockMvc.perform(patch("/api/v1/member/profile/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"links": [{"type": "GITHUB", "link": "https://github.com/new"}]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.profile.github").value("https://github.com/new"));

        // then
        mockMvc.perform(get("/api/v1/member/me/business-card/edit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.activity").doesNotExist())
            .andExpect(jsonPath("$.result.contact.email").value("contact@example.com"))
            .andExpect(jsonPath("$.result.links[?(@.type == 'GITHUB')].link").value("https://github.com/new"))
            .andExpect(jsonPath("$.result.links[?(@.type == 'GITHUB')].isPublic").value(false));
        mockMvc.perform(get("/api/v1/member/me/business-card"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.contact.email").value("contact@example.com"))
            .andExpect(jsonPath("$.result.links").isEmpty());
    }

    private void authenticate(Long memberId) {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(memberId)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
