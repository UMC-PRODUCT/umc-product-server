package com.umc.product.member.application.port.in.query.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.member.domain.LinkTypeAndLink;
import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.MemberProfileLinkType;

@DisplayName("MemberProfileInfo")
class MemberProfileInfoTest {

    @Test
    @DisplayName("publicFrom은 비공개로 설정한 링크를 null로 채운다")
    void publicFrom은_비공개로_설정한_링크를_null로_채운다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/umc"),
            new LinkTypeAndLink(MemberProfileLinkType.BLOG, "blog.example.com")
        ));
        profile.changeVisibility(null, Map.of(MemberProfileLinkType.GITHUB, false));

        // when
        MemberProfileInfo publicInfo = MemberProfileInfo.publicFrom(profile);

        // then
        assertThat(publicInfo.github()).isNull();
        // 기존 동작 호환을 위해 링크 형식은 검사하지 않는다.
        assertThat(publicInfo.blog()).isEqualTo("blog.example.com");
    }

    @Test
    @DisplayName("publicFrom은 프로필이 없으면 빈 프로필을 반환한다")
    void publicFrom은_프로필이_없으면_빈_프로필을_반환한다() {
        // when
        MemberProfileInfo publicInfo = MemberProfileInfo.publicFrom(null);

        // then
        assertThat(publicInfo).isEqualTo(MemberProfileInfo.builder().build());
    }
}
