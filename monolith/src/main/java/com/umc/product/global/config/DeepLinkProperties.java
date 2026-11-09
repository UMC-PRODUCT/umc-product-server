package com.umc.product.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 앱 딥링크(Universal Link/App Link) 공통 설정(#1268). 도메인을 모르는 값(호스트, 앱 식별자)만 갖고,
 * 도메인별 링크 경로는 각 도메인이 소유한다.
 *
 * <p>기본값은 application.yml 한 곳에만 둔다. env를 빈 값으로 넣는 실수로 깨진 링크나 검증 파일이
 * 조용히 나가지 않도록, 필수 값이 비어 있으면 기동 시점에 실패한다.</p>
 */
@Validated
@ConfigurationProperties(prefix = "app.deep-link")
public record DeepLinkProperties(
    @NotBlank String baseUrl,
    @Valid @NotNull Android android,
    @Valid @NotNull Ios ios
) {

    public DeepLinkProperties {
        baseUrl = stripTrailingSlash(baseUrl);
    }

    /** {@code path}는 {@code /}로 시작하는 경로이며, {@code {baseUrl}{path}} 형태의 절대 URL을 만든다. */
    public String webUrl(String path) {
        return baseUrl + path;
    }

    private static String stripTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        while (stripped.endsWith("/")) {
            stripped = stripped.substring(0, stripped.length() - 1);
        }
        return stripped;
    }

    public record Android(
        @NotBlank String packageName,
        @NotEmpty List<@NotBlank String> sha256CertFingerprints
    ) {

        public Android {
            sha256CertFingerprints = sha256CertFingerprints == null ? null : List.copyOf(sha256CertFingerprints);
        }

        public String playStoreUrl() {
            return "https://play.google.com/store/apps/details?id=" + packageName;
        }
    }

    public record Ios(
        @NotBlank String teamId,
        @NotBlank String bundleId,
        @NotBlank String appStoreUrl
    ) {

        /** apple-app-site-association의 {@code appID} 형식(TeamID.BundleID). */
        public String appId() {
            return teamId + "." + bundleId;
        }
    }
}
