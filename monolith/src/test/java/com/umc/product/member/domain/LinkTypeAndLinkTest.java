package com.umc.product.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("LinkTypeAndLink 도메인")
class LinkTypeAndLinkTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "https://github.com/umc",
        "http://blog.example.com/post?id=1",
        "HTTPS://LinkedIn.com/in/umc",
        "  https://umc.example.com  "
    })
    @DisplayName("http(s) 스킴과 점이 있는 호스트를 가진 링크는 열 수 있다")
    void http_스킴과_점이_있는_호스트를_가진_링크는_열_수_있다(String link) {
        assertThat(new LinkTypeAndLink(MemberProfileLinkType.GITHUB, link).isOpenable()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ",
        "github.com/umc",
        "javascript:alert(1)",
        "ftp://files.example.com",
        "https://localhost",
        "https://exa mple.com"
    })
    @DisplayName("스킴이 없거나 http(s)가 아니거나 형식이 잘못된 링크는 열 수 없다")
    void 스킴이_없거나_http가_아니거나_형식이_잘못된_링크는_열_수_없다(String link) {
        assertThat(new LinkTypeAndLink(MemberProfileLinkType.GITHUB, link).isOpenable()).isFalse();
    }
}
