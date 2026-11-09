package com.umc.product.community.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.global.config.DeepLinkProperties;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;

@WebMvcTest(controllers = CommunityThreadDeepLinkFallbackController.class)
@Import({JacksonConfig.class, CommunityThreadDeepLinkFallbackControllerTest.TestDeepLinkConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("커뮤니티 스레드 공유 링크 fallback 페이지")
class CommunityThreadDeepLinkFallbackControllerTest {

    private static final String ANDROID_UA = "Mozilla/5.0 (Linux; Android 14)";
    private static final String IOS_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X)";
    private static final String DESKTOP_UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)";
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.umc.product";
    private static final String APP_STORE_URL = "https://apps.apple.com/app/id6759412446";
    private static final String ANDROID_INTENT_URL =
        "intent://thread/42#Intent;scheme=umc;package=com.umc.product;S.browser_fallback_url="
            + "https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.umc.product;end";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Android User-Agent면 intent URL로 앱 실행을 자동 시도하고, 실패하면 Play 스토어로 이동한다")
    void show_androidUserAgent_autoLaunchesIntentUrlWithPlayStore() throws Exception {
        mockMvc.perform(get("/community/threads/42").header(HttpHeaders.USER_AGENT, ANDROID_UA))
            .andExpect(status().isOk())
            .andExpect(view().name("community/thread-deep-link"))
            .andExpect(model().attribute("platform", "ANDROID"))
            .andExpect(model().attribute("autoLaunch", true))
            .andExpect(model().attribute("appDeepLink", ANDROID_INTENT_URL))
            .andExpect(model().attribute("storeUrl", PLAY_STORE_URL))
            .andExpect(content().string(containsString("window.location.href = appUrl")))
            .andExpect(content().string(not(containsString(APP_STORE_URL))));
    }

    @Test
    @DisplayName("iOS User-Agent면 커스텀 스킴으로 앱 실행을 자동 시도하고, 실패하면 App Store로 이동한다")
    void show_iosUserAgent_autoLaunchesCustomSchemeWithAppStore() throws Exception {
        mockMvc.perform(get("/community/threads/42").header(HttpHeaders.USER_AGENT, IOS_UA))
            .andExpect(status().isOk())
            .andExpect(model().attribute("platform", "IOS"))
            .andExpect(model().attribute("autoLaunch", true))
            .andExpect(model().attribute("appDeepLink", "umc://thread/42"))
            .andExpect(model().attribute("storeUrl", APP_STORE_URL))
            .andExpect(content().string(containsString("window.location.href = appUrl")))
            .andExpect(content().string(not(containsString(PLAY_STORE_URL))));
    }

    @Test
    @DisplayName("데스크톱 User-Agent면 앱 실행을 시도하지 않고 App Store와 Play 스토어 링크를 모두 보여준다")
    void show_desktopUserAgent_rendersBothStoreLinks() throws Exception {
        mockMvc.perform(get("/community/threads/42").header(HttpHeaders.USER_AGENT, DESKTOP_UA))
            .andExpect(status().isOk())
            .andExpect(model().attribute("platform", "DESKTOP"))
            .andExpect(model().attribute("autoLaunch", false))
            .andExpect(content().string(containsString(APP_STORE_URL)))
            .andExpect(content().string(containsString(PLAY_STORE_URL)))
            .andExpect(content().string(not(containsString("window.location"))));
    }

    @Test
    @DisplayName("User-Agent가 없으면 UNKNOWN으로 처리하고 두 스토어 링크를 보여준다")
    void show_missingUserAgent_rendersBothStoreLinks() throws Exception {
        mockMvc.perform(get("/community/threads/42"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("platform", "UNKNOWN"))
            .andExpect(model().attribute("autoLaunch", false))
            .andExpect(content().string(containsString(APP_STORE_URL)))
            .andExpect(content().string(containsString(PLAY_STORE_URL)));
    }

    @Test
    @DisplayName("어떤 플랫폼에서도 홈페이지 안내 버튼을 보여주지 않는다")
    void show_neverRendersHomepageLink() throws Exception {
        for (String userAgent : new String[] {ANDROID_UA, IOS_UA, DESKTOP_UA}) {
            mockMvc.perform(get("/community/threads/42").header(HttpHeaders.USER_AGENT, userAgent))
                .andExpect(content().string(not(containsString("더 알아보기"))))
                .andExpect(content().string(not(containsString("university.neordinary.com"))));
        }
    }

    @TestConfiguration
    static class TestDeepLinkConfig {

        @Bean
        DeepLinkProperties deepLinkProperties() {
            return new DeepLinkProperties(
                "https://api.university.neordinary.com",
                new DeepLinkProperties.Android("com.umc.product", List.of("AA:BB")),
                new DeepLinkProperties.Ios("8B8B4462NV", "com.umc.product", APP_STORE_URL)
            );
        }
    }
}
