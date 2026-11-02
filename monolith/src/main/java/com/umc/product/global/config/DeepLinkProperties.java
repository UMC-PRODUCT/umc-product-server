package com.umc.product.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 앱 딥링크(Universal Link/App Link) 공통 설정. 커뮤니티 스레드 공유에서 시작했지만, 공지 등
 * 다른 도메인이 같은 base-url/검증 파일을 재사용할 수 있도록 global에 둔다(#1268).
 */
@ConfigurationProperties(prefix = "app.deep-link")
public record DeepLinkProperties(
    String baseUrl,
    String fallbackUrl,
    Android android,
    Ios ios
) {

    private static final String DEFAULT_FALLBACK_URL = "https://university.neordinary.com";

    public DeepLinkProperties {
        fallbackUrl = fallbackUrl == null || fallbackUrl.isBlank() ? DEFAULT_FALLBACK_URL : fallbackUrl;
        android = android == null ? Android.defaults() : android;
        ios = ios == null ? Ios.defaults() : ios;
    }

    /** {@code {baseUrl}/community/threads/{threadId}} 형태의 절대 URL을 만든다. */
    public String communityThreadShareUrl(Long threadId) {
        return baseUrl + "/community/threads/" + threadId;
    }

    public record Android(
        String packageName,
        List<String> sha256CertFingerprints
    ) {

        public Android {
            sha256CertFingerprints = sha256CertFingerprints == null ? List.of() : List.copyOf(sha256CertFingerprints);
        }

        static Android defaults() {
            return new Android(null, List.of());
        }

        public String playStoreUrl() {
            return "https://play.google.com/store/apps/details?id=" + packageName;
        }
    }

    public record Ios(
        String teamId,
        String bundleId,
        String appStoreUrl
    ) {

        static Ios defaults() {
            return new Ios(null, null, null);
        }

        /** apple-app-site-association의 {@code appID} 형식(TeamID.BundleID). */
        public String appId() {
            return teamId + "." + bundleId;
        }
    }
}
