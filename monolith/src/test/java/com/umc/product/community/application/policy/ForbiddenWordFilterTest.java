package com.umc.product.community.application.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

@DisplayName("ForbiddenWordFilter")
class ForbiddenWordFilterTest {

    private static final String CSV = """
        id,term,category
        H001,나쁜말,테스트
        H002,금지어,테스트
        """;

    private final ForbiddenWordFilter sut = new ForbiddenWordFilter(
        new ByteArrayResource(CSV.getBytes(StandardCharsets.UTF_8))
    );

    @Test
    @DisplayName("본문에 금칙어가 포함되면 true를 반환한다")
    void containsForbiddenWord_matchesSubstring() {
        assertThat(sut.containsForbiddenWord("너는 나쁜말을 썼어")).isTrue();
    }

    @Test
    @DisplayName("금칙어가 없는 본문은 false를 반환한다")
    void containsForbiddenWord_noMatch() {
        assertThat(sut.containsForbiddenWord("안녕하세요 반갑습니다")).isFalse();
    }

    @Test
    @DisplayName("본문이 null이거나 비어있으면 false를 반환한다")
    void containsForbiddenWord_blankContent() {
        assertThat(sut.containsForbiddenWord(null)).isFalse();
        assertThat(sut.containsForbiddenWord("")).isFalse();
        assertThat(sut.containsForbiddenWord("   ")).isFalse();
    }

    @Test
    @DisplayName("글자 사이에 공백을 끼워 넣어도 걸린다")
    void containsForbiddenWord_spacedOut() {
        assertThat(sut.containsForbiddenWord("너 나 쁜 말 하지마")).isTrue();
    }

    @Test
    @DisplayName("글자 사이에 기호를 끼워 넣어도 걸린다")
    void containsForbiddenWord_punctuationInserted() {
        assertThat(sut.containsForbiddenWord("나.쁜-말 하지마")).isTrue();
    }

    @Test
    @DisplayName("영문 금칙어는 대소문자를 바꿔도 걸린다")
    void containsForbiddenWord_caseInsensitive() {
        ForbiddenWordFilter english = new ForbiddenWordFilter(
            new ByteArrayResource("""
                id,term,category
                E001,badword,테스트
                """.getBytes(StandardCharsets.UTF_8))
        );

        assertThat(english.containsForbiddenWord("he is a BadWord")).isTrue();
    }

    @Test
    @DisplayName("실제 번들된 목록 파일을 정상적으로 불러온다")
    void load_actualBundledResource() {
        ForbiddenWordFilter actual = new ForbiddenWordFilter(
            new ClassPathResource("community/forbidden-words.csv")
        );

        assertThat(actual.containsForbiddenWord("짱깨라고 부르지 마세요")).isTrue();
        assertThat(actual.containsForbiddenWord("정상적인 메시지입니다")).isFalse();
    }
}
