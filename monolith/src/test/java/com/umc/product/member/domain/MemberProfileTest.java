package com.umc.product.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MemberProfile 도메인")
class MemberProfileTest {

    @Test
    @DisplayName("fromLinks는 링크 타입별 컬럼에 값을 매핑한다")
    void fromLinks는_링크_타입별_컬럼에_값을_매핑한다() {
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.LINKEDIN, "https://linkedin.com/in/umc"),
            new LinkTypeAndLink(MemberProfileLinkType.INSTAGRAM, "https://instagram.com/umc"),
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/umc"),
            new LinkTypeAndLink(MemberProfileLinkType.BLOG, "https://blog.example.com"),
            new LinkTypeAndLink(MemberProfileLinkType.PERSONAL, "https://umc.example.com")
        ));

        assertThat(profile.getLinkedIn()).isEqualTo("https://linkedin.com/in/umc");
        assertThat(profile.getInstagram()).isEqualTo("https://instagram.com/umc");
        assertThat(profile.getGithub()).isEqualTo("https://github.com/umc");
        assertThat(profile.getBlog()).isEqualTo("https://blog.example.com");
        assertThat(profile.getPersonal()).isEqualTo("https://umc.example.com");
    }

    @Test
    @DisplayName("updateLinks는 기존 링크를 모두 초기화한 뒤 제공된 링크만 반영한다")
    void updateLinks는_기존_링크를_모두_초기화한_뒤_제공된_링크만_반영한다() {
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/old"),
            new LinkTypeAndLink(MemberProfileLinkType.BLOG, "https://old.example.com")
        ));

        profile.updateLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.PERSONAL, "https://new.example.com")
        ));

        assertThat(profile.getGithub()).isNull();
        assertThat(profile.getBlog()).isNull();
        assertThat(profile.getPersonal()).isEqualTo("https://new.example.com");
    }

    @Test
    @DisplayName("새 프로필은 연락처가 비어 있고 연락처와 링크는 모두 공개가 초기값이다")
    void 새_프로필은_연락처가_비어_있고_연락처와_링크는_모두_공개가_초기값이다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of());

        // when & then
        assertThat(profile.getContactEmail()).isNull();
        assertThat(profile.isContactEmailPublic()).isTrue();
        assertThat(MemberProfileLinkType.values())
            .allSatisfy(type -> assertThat(profile.isLinkPublic(type)).isTrue());
    }

    @Test
    @DisplayName("changeVisibility는 값이 주어진 항목만 변경하고 나머지는 유지한다")
    void changeVisibility는_값이_주어진_항목만_변경하고_나머지는_유지한다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of());
        profile.changeVisibility(false, Map.of(MemberProfileLinkType.GITHUB, false));

        // when
        Map<MemberProfileLinkType, Boolean> onlyBlog = new HashMap<>();
        onlyBlog.put(MemberProfileLinkType.BLOG, false);
        onlyBlog.put(MemberProfileLinkType.INSTAGRAM, null);
        profile.changeVisibility(null, onlyBlog);

        // then
        assertThat(profile.isContactEmailPublic()).isFalse();
        assertThat(profile.isLinkPublic(MemberProfileLinkType.GITHUB)).isFalse();
        assertThat(profile.isLinkPublic(MemberProfileLinkType.BLOG)).isFalse();
        assertThat(profile.isLinkPublic(MemberProfileLinkType.INSTAGRAM)).isTrue();
        assertThat(profile.isLinkPublic(MemberProfileLinkType.LINKEDIN)).isTrue();
    }

    @Test
    @DisplayName("updateLinks로 링크를 교체해도 공개 여부는 유지된다")
    void updateLinks로_링크를_교체해도_공개_여부는_유지된다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/old")
        ));
        profile.changeVisibility(null, Map.of(MemberProfileLinkType.GITHUB, false));

        // when
        profile.updateLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/new")
        ));

        // then
        assertThat(profile.getLink(MemberProfileLinkType.GITHUB)).isEqualTo("https://github.com/new");
        assertThat(profile.isLinkPublic(MemberProfileLinkType.GITHUB)).isFalse();
    }

    @Test
    @DisplayName("연락처 이메일은 앞뒤 공백을 제거해 저장하고 비우면 삭제한다")
    void 연락처_이메일은_앞뒤_공백을_제거해_저장하고_비우면_삭제한다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of());

        // when
        profile.updateContactEmail("  contact@example.com ");
        assertThat(profile.getContactEmail()).isEqualTo("contact@example.com");

        // then
        profile.updateContactEmail(" ");
        assertThat(profile.getContactEmail()).isNull();
    }
}
