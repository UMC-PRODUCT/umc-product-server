package com.umc.product.notification.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.NotificationHistory;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.MemberFixture;

class NotificationHistoryQueryIntegrationTest extends IntegrationTestSupport {

    private static final String PATH = "/api/v1/notifications";
    private static final String IMAGE_URL = "https://example.com/notice.png";
    private static final String DEEP_LINK = "umc://notice/42";
    private static final Instant LATEST = Instant.parse("2026-11-27T00:00:00Z");

    @Autowired
    private MemberFixture members;

    @Autowired
    private SaveMemberPort saveMemberPort;

    @Autowired
    private SaveNotificationHistoryPort historyPort;

    @Test
    @DisplayName("본인 알림을 생성 시각과 ID로 정렬하고 이미지 URL과 딥링크를 반환한다")
    void 본인_알림에_대해_커서_페이지를_반환한다() throws Exception {
        // given
        Long memberId = members.일반("알림수신자").getId();
        Long otherId = members.일반("다른회원").getId();
        String token = login(memberId);
        saveHistory(memberId, "같은 시각 먼저 저장", LATEST, null, null);
        saveHistory(otherId, "다른 회원 알림", LATEST, null, null);
        saveHistory(memberId, "같은 시각 나중에 저장", LATEST, IMAGE_URL, DEEP_LINK);
        saveHistory(memberId, "이전 알림", LATEST.minusSeconds(30), null, null);

        // when
        String firstPage = mockMvc.perform(get(PATH).header("Authorization", token)
                .param("size", "1").param("memberId", otherId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.content[0].title").value("같은 시각 나중에 저장"))
            .andExpect(jsonPath("$.result.content[0].body").value("본문"))
            .andExpect(jsonPath("$.result.content[0].imageUrl").value(IMAGE_URL))
            .andExpect(jsonPath("$.result.content[0].deepLink").value(DEEP_LINK))
            .andExpect(jsonPath("$.result.content[0].createdAt").value(LATEST.toString()))
            .andExpect(jsonPath("$.result.hasNext").value(true))
            .andReturn().getResponse().getContentAsString();
        var result = objectMapper.readTree(firstPage).path("result");
        String nextCursor = result.path("nextCursor").asText();
        assertThat(nextCursor).isNotBlank().isEqualTo(result.path("content").get(0).path("notificationId").asText());

        // then
        mockMvc.perform(get(PATH).header("Authorization", token).param("cursor", nextCursor))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content.length()").value(2))
            .andExpect(jsonPath("$.result.content[0].title").value("같은 시각 먼저 저장"))
            .andExpect(jsonPath("$.result.content[0].imageUrl").value(nullValue()))
            .andExpect(jsonPath("$.result.content[0].deepLink").value(nullValue()))
            .andExpect(jsonPath("$.result.content[1].title").value("이전 알림"))
            .andExpect(jsonPath("$.result.hasNext").value(false))
            .andExpect(jsonPath("$.result.nextCursor").value(nullValue()));
    }

    @Test
    @DisplayName("다음 페이지를 보기 전에 새 알림이 저장되어도 기존 커서의 조회가 밀리지 않는다")
    void 새_알림이_추가되어도_기존_커서에서_이어서_조회한다() throws Exception {
        // given
        Long memberId = members.일반("알림수신자").getId();
        String token = login(memberId);
        saveHistory(memberId, "기존 첫 번째", LATEST, null, null);
        saveHistory(memberId, "기존 두 번째", LATEST.minusSeconds(10), null, null);
        saveHistory(memberId, "기존 세 번째", LATEST.minusSeconds(20), null, null);
        saveHistory(memberId, "기존 네 번째", LATEST.minusSeconds(30), null, null);
        String firstPage = mockMvc.perform(get(PATH).header("Authorization", token).param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content.length()").value(2))
            .andExpect(jsonPath("$.result.content[0].title").value("기존 첫 번째"))
            .andExpect(jsonPath("$.result.content[1].title").value("기존 두 번째"))
            .andExpect(jsonPath("$.result.hasNext").value(true))
            .andReturn().getResponse().getContentAsString();
        String cursor = objectMapper.readTree(firstPage).path("result").path("nextCursor").asText();
        assertThat(cursor).isNotBlank();

        // when
        saveHistory(memberId, "새 알림", LATEST.plusSeconds(10), null, null);

        // then
        mockMvc.perform(get(PATH).header("Authorization", token).param("size", "2").param("cursor", cursor))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content.length()").value(2))
            .andExpect(jsonPath("$.result.content[0].title").value("기존 세 번째"))
            .andExpect(jsonPath("$.result.content[1].title").value("기존 네 번째"))
            .andExpect(jsonPath("$.result.hasNext").value(false))
            .andExpect(jsonPath("$.result.nextCursor").value(nullValue()));
        mockMvc.perform(get(PATH).header("Authorization", token).param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].title").value("새 알림"));
    }

    @Test
    @DisplayName("다른 회원의 커서와 존재하지 않는 커서는 모두 빈 목록을 반환한다")
    void 다른_회원의_커서를_조회에_사용할_수_없다() throws Exception {
        // given
        Long memberId = members.일반("알림수신자").getId();
        Long otherId = members.일반("다른회원").getId();
        saveHistory(memberId, "본인 알림", LATEST.minusSeconds(30), null, null);
        saveHistory(otherId, "다른 회원 알림", LATEST, null, null);
        String otherPage = mockMvc.perform(get(PATH).header("Authorization", login(otherId)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String otherCursor = objectMapper.readTree(otherPage).path("result").path("content").get(0)
            .path("notificationId").asText();

        // when / then
        String token = login(memberId);
        assertEmptyPage(mockMvc.perform(get(PATH).header("Authorization", token).param("cursor", otherCursor)));
        assertEmptyPage(mockMvc.perform(get(PATH).header("Authorization", token)
            .param("cursor", Long.toString(Long.MAX_VALUE))));
    }

    @Test
    @DisplayName("인증 토큰 없이 호출하면 401을 반환한다")
    void 비로그인_요청을_거부한다() throws Exception {
        mockMvc.perform(get(PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원이 삭제되면 유효한 토큰이 남아 있어도 알림 이력을 조회할 수 없다")
    void 삭제된_회원의_기존_토큰으로_조회할_수_없다() throws Exception {
        // given
        Member member = members.일반("탈퇴회원");
        String token = login(member.getId());
        saveHistory(member.getId(), "탈퇴 전 받은 알림", LATEST, null, null);
        mockMvc.perform(get(PATH).header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].title").value("탈퇴 전 받은 알림"));

        // when
        saveMemberPort.delete(member);

        // then
        mockMvc.perform(get(PATH).header("Authorization", token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.result.content").doesNotExist());
    }

    @ParameterizedTest
    @CsvSource({"size, 0", "size, 101", "cursor, -1", "cursor, invalid"})
    @DisplayName("잘못된 페이지 크기나 커서는 400을 반환한다")
    void 잘못된_페이지_입력을_거부한다(String param, String value) throws Exception {
        Long memberId = members.일반("알림수신자").getId();
        mockMvc.perform(get(PATH).header("Authorization", login(memberId)).param(param, value))
            .andExpect(status().isBadRequest());
    }

    private void assertEmptyPage(ResultActions response) throws Exception {
        response.andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content").isEmpty())
            .andExpect(jsonPath("$.result.hasNext").value(false))
            .andExpect(jsonPath("$.result.nextCursor").value(nullValue()));
    }

    private String login(Long memberId) {
        String token = "notification-token-" + memberId;
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
        return "Bearer " + token;
    }

    private void saveHistory(Long memberId, String title, Instant createdAt, String imageUrl, String deepLink) {
        historyPort.saveIfAbsent(
            new NotificationHistory(UUID.randomUUID(), title, "본문", imageUrl, deepLink, createdAt),
            List.of(memberId)
        );
    }
}
