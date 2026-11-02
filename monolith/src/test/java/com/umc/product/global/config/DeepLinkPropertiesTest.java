package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("앱 딥링크 설정")
class DeepLinkPropertiesTest {

    @Test
    @DisplayName("base-url과 threadId로 커뮤니티 스레드 공유 URL을 만든다")
    void communityThreadShareUrl_buildsAbsoluteUrl() {
        DeepLinkProperties properties = new DeepLinkProperties(
            "https://api.university.neordinary.com", null, null, null
        );

        assertThat(properties.communityThreadShareUrl(42L))
            .isEqualTo("https://api.university.neordinary.com/community/threads/42");
    }

    @Test
    @DisplayName("fallback-url이 비어 있으면 기본 홈페이지로 대체한다")
    void fallbackUrl_defaultsWhenBlank() {
        DeepLinkProperties properties = new DeepLinkProperties("https://api.university.neordinary.com", "", null, null);

        assertThat(properties.fallbackUrl()).isEqualTo("https://university.neordinary.com");
    }

    @Test
    @DisplayName("android/ios 서브 설정이 없으면 빈 기본값으로 채운다")
    void subProperties_defaultWhenNull() {
        DeepLinkProperties properties = new DeepLinkProperties("https://api.university.neordinary.com", null, null, null);

        assertThat(properties.android().sha256CertFingerprints()).isEmpty();
        assertThat(properties.android().packageName()).isNull();
        assertThat(properties.ios().teamId()).isNull();
    }

    @Test
    @DisplayName("android playStoreUrl은 패키지명으로 Play 스토어 상세 링크를 만든다")
    void android_playStoreUrl() {
        DeepLinkProperties.Android android = new DeepLinkProperties.Android("com.umc.product", List.of("AA:BB"));

        assertThat(android.playStoreUrl())
            .isEqualTo("https://play.google.com/store/apps/details?id=com.umc.product");
    }

    @Test
    @DisplayName("ios appId는 TeamID.BundleID 형식이다")
    void ios_appId() {
        DeepLinkProperties.Ios ios = new DeepLinkProperties.Ios("8B8B4462NV", "com.umc.product", null);

        assertThat(ios.appId()).isEqualTo("8B8B4462NV.com.umc.product");
    }
}
