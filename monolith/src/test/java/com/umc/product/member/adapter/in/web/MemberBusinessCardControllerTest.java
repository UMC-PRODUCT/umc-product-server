package com.umc.product.member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

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

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.application.port.in.command.EditBusinessCardUseCase;
import com.umc.product.member.application.port.in.command.dto.EditBusinessCardCommand;
import com.umc.product.member.application.port.in.query.GetBusinessCardUseCase;
import com.umc.product.member.application.port.in.query.dto.BusinessCardEditInfo;
import com.umc.product.member.application.port.in.query.dto.BusinessCardInfo;
import com.umc.product.member.domain.MemberProfileLinkType;

@WebMvcTest(controllers = MemberBusinessCardController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MemberBusinessCardController")
class MemberBusinessCardControllerTest {

    private static final Long TEST_MEMBER_ID = 99L;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    GetBusinessCardUseCase getBusinessCardUseCase;

    @MockitoBean
    EditBusinessCardUseCase editBusinessCardUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(TEST_MEMBER_ID)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("내 명함을 명함 수정 요청과 같은 모양의 연락처·링크로 반환한다")
    void 내_명함을_명함_수정_요청과_같은_모양의_연락처_링크로_반환한다() throws Exception {
        // given
        given(getBusinessCardUseCase.getByMemberId(TEST_MEMBER_ID)).willReturn(new BusinessCardInfo(
            TEST_MEMBER_ID,
            "홍길동",
            "길동",
            "UMC대학교",
            null,
            new BusinessCardInfo.Activity(9L, 9L, ChallengerPart.SPRINGBOOT),
            new BusinessCardInfo.Contact("contact@example.com", true),
            List.of(new BusinessCardInfo.Link(MemberProfileLinkType.GITHUB, "https://github.com/umc", true))
        ));

        // when & then
        mockMvc.perform(get("/api/v1/member/me/business-card"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.memberId").value(TEST_MEMBER_ID))
            .andExpect(jsonPath("$.result.name").value("홍길동"))
            .andExpect(jsonPath("$.result.schoolName").value("UMC대학교"))
            .andExpect(jsonPath("$.result.activity.generation").value(9))
            .andExpect(jsonPath("$.result.activity.part").value("SPRINGBOOT"))
            .andExpect(jsonPath("$.result.contact.email").value("contact@example.com"))
            .andExpect(jsonPath("$.result.contact.isPublic").value(true))
            .andExpect(jsonPath("$.result.links[0].type").value("GITHUB"))
            .andExpect(jsonPath("$.result.links[0].link").value("https://github.com/umc"))
            .andExpect(jsonPath("$.result.links[0].isPublic").value(true));
    }

    @Test
    @DisplayName("활동 중이 아니거나 연락처가 비공개면 activity와 contact를 null로 반환한다")
    void 활동_중이_아니거나_연락처가_비공개면_activity와_contact를_null로_반환한다() throws Exception {
        // given
        given(getBusinessCardUseCase.getByMemberId(TEST_MEMBER_ID)).willReturn(new BusinessCardInfo(
            TEST_MEMBER_ID, "홍길동", "길동", "UMC대학교", null, null, null, List.of()
        ));

        // when & then
        mockMvc.perform(get("/api/v1/member/me/business-card"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.activity").doesNotExist())
            .andExpect(jsonPath("$.result.contact").doesNotExist())
            .andExpect(jsonPath("$.result.links").isEmpty());
    }

    @Test
    @DisplayName("명함 수정 화면은 연동 소셜과 5개 링크를 공개 여부와 함께 반환한다")
    void 명함_수정_화면은_연동_소셜과_5개_링크를_공개_여부와_함께_반환한다() throws Exception {
        // given
        given(getBusinessCardUseCase.getEditByMemberId(TEST_MEMBER_ID)).willReturn(editInfo());

        // when & then
        mockMvc.perform(get("/api/v1/member/me/business-card/edit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.name").value("홍길동"))
            .andExpect(jsonPath("$.result.activity.part").value("SPRINGBOOT"))
            .andExpect(jsonPath("$.result.oauthProviders[0]").value("GOOGLE"))
            .andExpect(jsonPath("$.result.contact.email").doesNotExist())
            .andExpect(jsonPath("$.result.contact.isPublic").value(true))
            .andExpect(jsonPath("$.result.links.length()").value(5))
            .andExpect(jsonPath("$.result.links[2].type").value("GITHUB"))
            .andExpect(jsonPath("$.result.links[2].isPublic").value(false));
    }

    @Test
    @DisplayName("명함 수정 요청을 커맨드로 변환하고 수정 화면과 같은 응답을 반환한다")
    void 명함_수정_요청을_커맨드로_변환하고_수정_화면과_같은_응답을_반환한다() throws Exception {
        // given
        given(getBusinessCardUseCase.getEditByMemberId(TEST_MEMBER_ID)).willReturn(editInfo());

        // when
        mockMvc.perform(patch("/api/v1/member/me/business-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "profileImageId": "image-1",
                      "contact": {"email": "contact@example.com", "isPublic": false},
                      "links": [{"type": "GITHUB", "link": "https://github.com/umc", "isPublic": false}]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.links.length()").value(5));

        // then
        ArgumentCaptor<EditBusinessCardCommand> captor = ArgumentCaptor.forClass(EditBusinessCardCommand.class);
        then(editBusinessCardUseCase).should().edit(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new EditBusinessCardCommand(
            TEST_MEMBER_ID,
            "image-1",
            new EditBusinessCardCommand.Contact("contact@example.com", false),
            List.of(new EditBusinessCardCommand.Link(MemberProfileLinkType.GITHUB, "https://github.com/umc", false))
        ));
    }

    @Test
    @DisplayName("명함 수정 요청에 공개 여부가 없으면 400을 반환한다")
    void 명함_수정_요청에_공개_여부가_없으면_400을_반환한다() throws Exception {
        assertBadRequest("""
            {
              "contact": {"email": "contact@example.com", "isPublic": true},
              "links": [{"type": "GITHUB", "link": "https://github.com/umc"}]
            }
            """);
    }

    @Test
    @DisplayName("명함 수정 요청에 연락처가 없으면 400을 반환한다")
    void 명함_수정_요청에_연락처가_없으면_400을_반환한다() throws Exception {
        assertBadRequest("""
            {"links": []}
            """);
    }

    @Test
    @DisplayName("명함 수정 요청의 연락처가 이메일 형식이 아니면 400을 반환한다")
    void 명함_수정_요청의_연락처가_이메일_형식이_아니면_400을_반환한다() throws Exception {
        assertBadRequest("""
            {"contact": {"email": "not-an-email", "isPublic": true}, "links": []}
            """);
    }

    @Test
    @DisplayName("명함 수정 요청의 링크 타입이 중복되면 400을 반환한다")
    void 명함_수정_요청의_링크_타입이_중복되면_400을_반환한다() throws Exception {
        assertBadRequest("""
            {
              "contact": {"isPublic": true},
              "links": [
                {"type": "GITHUB", "link": "https://github.com/a", "isPublic": true},
                {"type": "GITHUB", "link": "https://github.com/b", "isPublic": false}
              ]
            }
            """);
    }

    private void assertBadRequest(String body) throws Exception {
        // when
        mockMvc.perform(patch("/api/v1/member/me/business-card")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());

        // then
        then(editBusinessCardUseCase).should(never()).edit(any());
    }

    private BusinessCardEditInfo editInfo() {
        return new BusinessCardEditInfo(
            TEST_MEMBER_ID,
            "홍길동",
            "UMC대학교",
            null,
            new BusinessCardInfo.Activity(9L, 9L, ChallengerPart.SPRINGBOOT),
            List.of(OAuthProvider.GOOGLE),
            new BusinessCardInfo.Contact(null, true),
            Arrays.stream(MemberProfileLinkType.values())
                .map(type -> new BusinessCardInfo.Link(type, null, type != MemberProfileLinkType.GITHUB))
                .toList()
        );
    }
}
