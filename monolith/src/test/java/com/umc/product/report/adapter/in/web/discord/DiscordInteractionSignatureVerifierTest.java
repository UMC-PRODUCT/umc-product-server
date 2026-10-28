package com.umc.product.report.adapter.in.web.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Discord 상호작용 서명 검증")
class DiscordInteractionSignatureVerifierTest {

    private static final Instant NOW = Instant.parse("2026-10-29T00:00:00Z");
    private static final String TIMESTAMP = String.valueOf(NOW.getEpochSecond());
    private static final String BODY = "{\"type\":1}";

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final DiscordInteractionTestKeys keys = DiscordInteractionTestKeys.generate();
    private final DiscordInteractionSignatureVerifier sut = verifierWith(keys.publicKeyHex());

    @Test
    @DisplayName("공개키에 맞는 서명이면 통과한다")
    void isValid_올바른_서명이면_통과한다() {
        // given
        String signature = keys.sign(TIMESTAMP, BODY);

        // when
        boolean valid = sut.isValid(signature, TIMESTAMP, bytes(BODY));

        // then
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("body가 한 글자라도 바뀌면 거절한다")
    void isValid_body가_바뀌면_거절한다() {
        // given
        String signature = keys.sign(TIMESTAMP, BODY);

        // when
        boolean valid = sut.isValid(signature, TIMESTAMP, bytes("{\"type\":2}"));

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("다른 키로 만든 서명은 거절한다")
    void isValid_다른_키의_서명이면_거절한다() {
        // given
        String signature = DiscordInteractionTestKeys.generate().sign(TIMESTAMP, BODY);

        // when
        boolean valid = sut.isValid(signature, TIMESTAMP, bytes(BODY));

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("타임스탬프가 허용 범위를 벗어나면 서명이 맞아도 거절한다")
    void isValid_오래된_타임스탬프면_거절한다() {
        // given
        long staleEpochSecond = NOW.minus(DiscordInteractionSignatureVerifier.MAX_CLOCK_SKEW)
            .minusSeconds(1)
            .getEpochSecond();
        String staleTimestamp = String.valueOf(staleEpochSecond);
        String signature = keys.sign(staleTimestamp, BODY);

        // when
        boolean valid = sut.isValid(signature, staleTimestamp, bytes(BODY));

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("허용 범위 경계의 타임스탬프는 통과한다")
    void isValid_경계의_타임스탬프는_통과한다() {
        // given
        String edgeTimestamp = String.valueOf(
            NOW.minus(DiscordInteractionSignatureVerifier.MAX_CLOCK_SKEW).getEpochSecond()
        );
        String signature = keys.sign(edgeTimestamp, BODY);

        // when
        boolean valid = sut.isValid(signature, edgeTimestamp, bytes(BODY));

        // then
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("서명이나 타임스탬프 형식이 잘못되었거나 없으면 거절한다")
    void isValid_형식이_잘못되면_거절한다() {
        // given
        String signature = keys.sign(TIMESTAMP, BODY);

        // when & then
        assertThat(sut.isValid("not-hex", TIMESTAMP, bytes(BODY))).isFalse();
        assertThat(sut.isValid("abcd", TIMESTAMP, bytes(BODY))).isFalse();
        assertThat(sut.isValid(signature, "not-a-number", bytes(BODY))).isFalse();
        assertThat(sut.isValid(null, TIMESTAMP, bytes(BODY))).isFalse();
        assertThat(sut.isValid(signature, null, bytes(BODY))).isFalse();
        assertThat(sut.isValid(signature, TIMESTAMP, null)).isFalse();
    }

    @Test
    @DisplayName("공개키가 설정되지 않았으면 모든 요청을 거절한다")
    void isValid_공개키가_없으면_거절한다() {
        // given
        DiscordInteractionSignatureVerifier unconfigured = verifierWith("");
        String signature = keys.sign(TIMESTAMP, BODY);

        // when
        boolean valid = unconfigured.isValid(signature, TIMESTAMP, bytes(BODY));

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("공개키 형식이 잘못되었으면 시작할 때 실패한다")
    void 생성_공개키_형식이_잘못되면_실패한다() {
        assertThatThrownBy(() -> verifierWith("abcd"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageNotContaining("abcd");
    }

    private DiscordInteractionSignatureVerifier verifierWith(String publicKeyHex) {
        return new DiscordInteractionSignatureVerifier(new ReportDiscordInteractionProperties(publicKeyHex), clock);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
