package com.umc.product.community.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

import com.umc.product.global.config.SecurityPathConfig;

@DisplayName("커뮤니티 스레드 링크")
class CommunityThreadLinksTest {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Test
    @DisplayName("공유 웹 경로와 앱 커스텀 스킴 URL을 만든다")
    void webPathAndAppUrl() {
        assertThat(CommunityThreadLinks.webPath(42L)).isEqualTo("/community/threads/42");
        assertThat(CommunityThreadLinks.appUrl(42L)).isEqualTo("umc://thread/42");
    }

    @Test
    @DisplayName("Android intent URL은 커스텀 스킴과 같은 화면을 가리키고 fallback URL을 인코딩해 담는다")
    void androidIntentUrl() {
        assertThat(CommunityThreadLinks.androidIntentUrl(42L, "com.umc.product", "https://example.com/a?id=1"))
            .isEqualTo("intent://thread/42#Intent;scheme=umc;package=com.umc.product;"
                + "S.browser_fallback_url=https%3A%2F%2Fexample.com%2Fa%3Fid%3D1;end");
    }

    @Test
    @DisplayName("fallback 페이지 경로와 AASA 경로 패턴이 공유 웹 경로와 일치한다")
    void mappingAndAppLinkPatternMatchWebPath() {
        String webPath = CommunityThreadLinks.webPath(42L);

        assertThat(PATH_MATCHER.match(CommunityThreadLinks.WEB_PATH_TEMPLATE, webPath)).isTrue();
        assertThat(PATH_MATCHER.match(CommunityThreadLinks.APP_LINK_PATH_PATTERN, webPath)).isTrue();
    }

    @Test
    @DisplayName("점검 중에도 공유 링크가 열리도록 점검 예외 경로가 공유 웹 경로를 포함한다")
    void maintenanceAllowPathCoversWebPath() {
        String webPath = CommunityThreadLinks.webPath(42L);

        assertThat(SecurityPathConfig.MAINTENANCE_ALWAYS_ALLOW_PATHS)
            .anyMatch(pattern -> PATH_MATCHER.match(pattern, webPath));
    }
}
