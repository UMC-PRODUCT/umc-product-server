package com.umc.product.member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.SaveSchoolPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("학교별 닉네임 사용 가능 여부 공개 API")
@TestPropertySource(properties = "app.api-rate-limit.enabled=false")
class MemberNicknameQueryControllerIntegrationTest extends IntegrationTestSupport {

    private static final String PATH = "/api/v1/member/nicknames/availability";

    @Autowired
    SchoolFixture schoolFixture;

    @Autowired
    GisuFixture gisuFixture;

    @Autowired
    ChallengerFixture challengerFixture;

    @Autowired
    SaveMemberPort saveMemberPort;

    @Autowired
    SaveSchoolPort saveSchoolPort;

    @Test
    @DisplayName("학교 로고 파일이 없어도 닉네임 사용 가능 여부를 조회한다")
    void 학교_로고_조회에_의존하지_않는다() throws Exception {
        // given
        School school = schoolFixture.학교("로고대학교");
        school.updateLogoImageId("missing-school-logo");
        saveSchoolPort.save(school);

        // when & then
        mockMvc.perform(get(PATH)
                .param("schoolId", school.getId().toString()).param("nickname", "토리"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.available").value(true));
    }

    @Test
    @DisplayName("로그인 없이 미사용 닉네임을 확인하고 사용 가능 여부만 응답한다")
    void 미사용_닉네임은_로그인_없이_조회한다() throws Exception {
        // given
        School school = schoolFixture.학교("조회대학교");

        // when
        var response = mockMvc.perform(get(PATH)
                .param("schoolId", school.getId().toString()).param("nickname", "토리"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.available").value(true))
            .andReturn().getResponse();

        // then
        var result = objectMapper.readTree(response.getContentAsString()).get("result");
        assertThat(result.size()).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(MemberStatus.class)
    @DisplayName("챌린저 이력 없이도 활동·휴면·탈퇴 회원의 닉네임을 사용 불가로 판정한다")
    void 모든_상태의_저장_회원을_포함한다(MemberStatus memberStatus) throws Exception {
        // given
        School school = schoolFixture.학교("상태대학교");
        Member member = Member.create("기존회원", "토리", "existing@test.com", school.getId(), null);
        ReflectionTestUtils.setField(member, "status", memberStatus);
        saveMemberPort.save(member);

        // when & then
        mockMvc.perform(get(PATH)
                .param("schoolId", school.getId().toString()).param("nickname", "토리"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.available").value(false));
    }

    @Test
    @DisplayName("다른 학교에서만 사용 중인 닉네임은 사용 가능하다")
    void 학교가_다르면_같은_닉네임을_사용한다() throws Exception {
        // given
        School first = schoolFixture.학교("첫대학교");
        School second = schoolFixture.학교("둘대학교");
        saveMemberPort.save(Member.create("기존회원", "토리", "other@test.com", first.getId(), null));

        // when & then
        mockMvc.perform(get(PATH)
                .param("schoolId", second.getId().toString()).param("nickname", "토리"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.available").value(true));
    }

    @Test
    @DisplayName("같은 학교의 기존 중복 회원이 여러 명이어도 오류 없이 사용 불가로 판정한다")
    void 기존_복수_중복도_조회한다() throws Exception {
        // given
        School school = schoolFixture.학교("중복대학교");
        saveMemberPort.save(Member.create("첫회원", "토리", "first@test.com", school.getId(), null));
        saveMemberPort.save(Member.create("둘회원", "토리", "second@test.com", school.getId(), null));

        // when & then
        mockMvc.perform(get(PATH)
                .param("schoolId", school.getId().toString()).param("nickname", "토리"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.available").value(false));
    }

    @Test
    @DisplayName("이전 비활성 기수의 챌린저 닉네임도 사용 불가로 판정한다")
    void 이전_기수도_포함한다() throws Exception {
        // given
        School school = schoolFixture.학교("이전대학교");
        Gisu previous = gisuFixture.비활성_기수(10L);
        Member member = saveMemberPort.save(
            Member.create("이전회원", "토리", "previous@test.com", school.getId(), null));
        challengerFixture.웹(member.getId(), previous.getId());

        // when & then
        mockMvc.perform(get(PATH)
                .param("schoolId", school.getId().toString()).param("nickname", "토리"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.available").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"가", "가나다라마바"})
    @DisplayName("기존 가입 규칙의 한글 한 자와 여섯 자 경계를 허용한다")
    void 기존_입력_길이를_유지한다(String nickname) throws Exception {
        // given
        School school = schoolFixture.학교("길이대학교");

        // when & then
        mockMvc.perform(get(PATH)
                .param("schoolId", school.getId().toString()).param("nickname", nickname))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.available").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", " 토리", "토리 ", "토 리", "tory", "토리1", "ㄱ", "가나다라마바사"})
    @DisplayName("빈 값·공백·한글 외 문자·길이 초과 닉네임은 거절한다")
    void 잘못된_닉네임은_거절한다(String nickname) throws Exception {
        // when & then
        mockMvc.perform(get(PATH).param("schoolId", "1").param("nickname", nickname))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("필수 학교와 닉네임이 누락되면 거절한다")
    void 필수_값_누락은_거절한다() throws Exception {
        // when & then
        mockMvc.perform(get(PATH).param("nickname", "토리"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get(PATH).param("schoolId", "1"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "invalid"})
    @DisplayName("양의 정수가 아닌 학교 ID는 거절한다")
    void 잘못된_학교_ID는_거절한다(String schoolId) throws Exception {
        // when & then
        mockMvc.perform(get(PATH).param("schoolId", schoolId).param("nickname", "토리"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 학교는 사용 가능이 아닌 학교 없음으로 응답한다")
    void 없는_학교는_거절한다() throws Exception {
        // when & then
        mockMvc.perform(get(PATH).param("schoolId", "9223372036854775807").param("nickname", "토리"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ORGANIZATION-0018"));
    }
}
