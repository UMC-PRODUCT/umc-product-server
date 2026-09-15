package com.umc.product.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmailMasker — 이메일 마스킹 유틸")
class EmailMaskerTest {

    @Test
    void null_입력은_null을_반환한다() {
        assertThat(EmailMasker.mask(null)).isNull();
    }

    @Test
    void 빈_문자열은_그대로_반환한다() {
        assertThat(EmailMasker.mask("")).isEqualTo("");
        assertThat(EmailMasker.mask("   ")).isEqualTo("   ");
    }

    @Test
    void 골뱅이가_없는_입력은_원문_그대로_반환한다() {
        // 비정상 입력에 대한 방어 — 마스킹 시도하지 않습니다.
        assertThat(EmailMasker.mask("notAnEmail")).isEqualTo("notAnEmail");
    }

    @Test
    void 로컬_파트가_비어있으면_원문_그대로_반환한다() {
        assertThat(EmailMasker.mask("@domain.com")).isEqualTo("@domain.com");
    }

    @Test
    void 로컬_파트_길이_1은_그대로_반환한다() {
        assertThat(EmailMasker.mask("a@umc.com")).isEqualTo("a@umc.com");
    }

    @Test
    void 로컬_파트_길이_2는_앞_1글자만_남기고_마스킹한다() {
        assertThat(EmailMasker.mask("ab@umc.com")).isEqualTo("a*@umc.com");
    }

    @Test
    void 로컬_파트_길이_3은_앞_1글자만_남기고_마스킹한다() {
        assertThat(EmailMasker.mask("abc@umc.com")).isEqualTo("a**@umc.com");
    }

    @Test
    void 로컬_파트_길이_4는_앞_3글자만_남기고_마스킹한다() {
        assertThat(EmailMasker.mask("abcd@umc.com")).isEqualTo("abc*@umc.com");
    }

    @Test
    void 로컬_파트가_긴_경우_앞_3글자만_남기고_마스킹한다() {
        assertThat(EmailMasker.mask("donggukcd200@gmail.com"))
            .isEqualTo("don*********@gmail.com");
    }

    @Test
    void 도메인은_절대_마스킹되지_않는다() {
        String masked = EmailMasker.mask("longlocalpart@hanyang.ac.kr");
        assertThat(masked).endsWith("@hanyang.ac.kr");
    }
}
