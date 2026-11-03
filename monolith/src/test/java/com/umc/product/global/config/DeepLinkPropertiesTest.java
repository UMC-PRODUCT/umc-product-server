package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

@DisplayName("앱 딥링크 설정")
class DeepLinkPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfiguration.class)
        .withPropertyValues(
            "app.deep-link.base-url=https://api.university.neordinary.com",
            "app.deep-link.fallback-url=https://university.neordinary.com",
            "app.deep-link.android.package-name=com.umc.product",
            "app.deep-link.android.sha256-cert-fingerprints=AA:BB, CC:DD",
            "app.deep-link.ios.team-id=8B8B4462NV",
            "app.deep-link.ios.bundle-id=com.umc.product",
            "app.deep-link.ios.app-store-url=https://apps.apple.com/app/id6759412446"
        );

    @Test
    @DisplayName("필수 값이 모두 있으면 바인딩되고, 콤마로 구분한 인증서 지문을 목록으로 읽는다")
    void bind_validProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            DeepLinkProperties properties = context.getBean(DeepLinkProperties.class);

            assertThat(properties.android().sha256CertFingerprints()).containsExactly("AA:BB", "CC:DD");
            assertThat(properties.ios().appId()).isEqualTo("8B8B4462NV.com.umc.product");
            assertThat(properties.android().playStoreUrl())
                .isEqualTo("https://play.google.com/store/apps/details?id=com.umc.product");
            assertThat(properties.ios().appStoreUrl()).isEqualTo("https://apps.apple.com/app/id6759412446");
        });
    }

    @Test
    @DisplayName("base-url 끝의 슬래시를 제거해 이중 슬래시 없는 절대 URL을 만든다")
    void webUrl_stripsTrailingSlash() {
        contextRunner.withPropertyValues("app.deep-link.base-url=https://api.university.neordinary.com/")
            .run(context -> assertThat(context.getBean(DeepLinkProperties.class).webUrl("/community/threads/42"))
                .isEqualTo("https://api.university.neordinary.com/community/threads/42"));
    }

    @Test
    @DisplayName("필수 값이 빈 문자열이면 기동에 실패한다")
    void requiredBlank_failsOnStartup() {
        for (String blank : new String[] {
            "app.deep-link.base-url=",
            "app.deep-link.base-url=/",
            "app.deep-link.fallback-url=",
            "app.deep-link.android.package-name=",
            "app.deep-link.ios.team-id=",
            "app.deep-link.ios.bundle-id=",
            "app.deep-link.ios.app-store-url="
        }) {
            contextRunner.withPropertyValues(blank)
                .run(context -> assertThat(context).as(blank).hasFailed());
        }
    }

    @Test
    @DisplayName("Android 인증서 지문이 비어 있으면 기동에 실패한다")
    void emptyFingerprints_failsOnStartup() {
        contextRunner.withPropertyValues("app.deep-link.android.sha256-cert-fingerprints=")
            .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("android/ios 설정 블록이 없으면 기동에 실패한다")
    void missingSubProperties_failsOnStartup() {
        new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                "app.deep-link.base-url=https://api.university.neordinary.com",
                "app.deep-link.fallback-url=https://university.neordinary.com"
            )
            .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(DeepLinkProperties.class)
    static class TestConfiguration {
    }
}
