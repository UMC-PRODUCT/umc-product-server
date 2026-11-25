package com.umc.product.member.adapter.in.web;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultActions;

import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.SchoolFixture;

@TestPropertySource(properties = "app.api-rate-limit.enabled=false")
class MemberEmailLookupIntegrationTest extends IntegrationTestSupport {

    @Autowired
    SchoolFixture schoolFixture;

    @Autowired
    SaveMemberPort saveMemberPort;

    @Autowired
    SaveMemberOAuthPort saveMemberOAuthPort;

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void 로그인_없이_마스킹된_이메일과_가입일과_연동_계정을_확인한다(int count) throws Exception {
        // given
        Long schoolId = schoolFixture.학교("조회대학교").getId();
        Member member = Member.create("김유엠", "움씨", "2eabc3@example.org", schoolId, null);
        member.registerCredential("test-password-hash");
        saveMemberPort.save(member);
        var providers = List.of(OAuthProvider.KAKAO, OAuthProvider.APPLE, OAuthProvider.GOOGLE).subList(0, count);
        for (OAuthProvider provider : providers.reversed()) {
            saveMemberOAuthPort.save(MemberOAuth.builder().memberId(member.getId())
                .provider(provider).providerId("test-" + provider.name()).build());
        }

        // when & then
        lookup(schoolId, "김유엠", "움씨").andExpect(status().isOk())
            .andExpect(jsonPath("$.result", aMapWithSize(3)))
            .andExpect(jsonPath("$.result.maskedEmail").value("2e***3@example.org"))
            .andExpect(jsonPath("$.result.joinedOn").value(
                LocalDate.ofInstant(member.getCreatedAt(), ZoneId.of("Asia/Seoul")).toString()))
            .andExpect(jsonPath("$.result.linkedProviders").value(providers.stream().map(Enum::name).toList()));
    }

    @Test
    void 조건이_다르거나_회원이_여러_명이면_같은_오류를_반환한다() throws Exception {
        // given
        Long schoolId = schoolFixture.학교("조회대학교").getId();
        saveMemberPort.save(Member.create("김유엠", "움씨", "first@example.org", schoolId, null));

        // when & then
        expectMissing(schoolId, "김유", "움씨");
        expectMissing(schoolId, "김유엠", "움");
        expectMissing(schoolId + 1, "김유엠", "움씨");
        saveMemberPort.save(Member.create("김유엠", "움씨", "other@example.org", schoolId, null));
        expectMissing(schoolId, "김유엠", "움씨");
    }

    @Test
    void 탈퇴한_회원의_정보는_공개하지_않는다() throws Exception {
        // given
        Long schoolId = schoolFixture.학교("조회대학교").getId();
        Member member = Member.create("김유엠", "움씨", "withdrawn@example.org", schoolId, null);
        ReflectionTestUtils.setField(member, "status", MemberStatus.WITHDRAWN);
        saveMemberPort.save(member);

        // when & then
        expectMissing(schoolId, "김유엠", "움씨");
    }

    @ParameterizedTest
    @CsvSource({"a@example.org,***@example.org", "ab@example.org,***@example.org", "abc@example.org,a***@example.org"})
    void 짧은_이메일도_원문을_가린다(String email, String maskedEmail) throws Exception {
        // given
        Long schoolId = schoolFixture.학교("조회대학교").getId();
        saveMemberPort.save(Member.create("김유엠", "움씨", email, schoolId, null));

        // when & then
        lookup(schoolId, "김유엠", "움씨").andExpect(status().isOk())
            .andExpect(jsonPath("$.result.maskedEmail").value(maskedEmail));
    }

    @ParameterizedTest
    @CsvSource({"0,김유엠,움씨", "1,' ',움씨", "1,김유엠,abc", "1,김유엠,가나다라마"})
    void 잘못된_입력은_거절한다(long schoolId, String name, String nickname) throws Exception {
        lookup(schoolId, name, nickname).andExpect(status().isBadRequest());
    }

    private void expectMissing(Long schoolId, String name, String nickname) throws Exception {
        lookup(schoolId, name, nickname).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("MEMBER-0001"));
    }

    private ResultActions lookup(Long schoolId, String name, String nickname) throws Exception {
        return mockMvc.perform(post("/api/v1/member/email-lookup").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of("schoolId", schoolId, "name", name, "nickname", nickname))));
    }
}
