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

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Android User-Agent면 앱이 없을 때 Play 스토어로 넘어가는 intent URL과 Play 스토어 버튼을 보여준다")
    void show_androidUserAgent_rendersIntentUrlAndPlayStore() throws Exception {
        mockMvc.perform(get("/community/threads/42").header(HttpHeaders.USER_AGENT, ANDROID_UA))
            .andExpect(status().isOk())
            .andExpect(view().name("community/thread-deep-link"))
            .andExpect(model().attribute("platform", "ANDROID"))
            .andExpect(model().attribute("appDeepLink",
                "intent://thread/42#Intent;scheme=umc;package=com.umc.product;S.browser_fallback_url="
                    + "https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.umc.product;end"))
            .andExpect(content().string(containsString("Play 스토어에서 다운로드")));
    }

    @Test
    @DisplayName("iOS User-Agent면 커스텀 스킴을 쓰고, 앱스토어 URL이 없으면 홈페이지 안내를 보여준다")
    void show_iosUserAgent_rendersCustomSchemeAndFallback() throws Exception {
        mockMvc.perform(get("/community/threads/42").header(HttpHeaders.USER_AGENT, IOS_UA))
            .andExpect(status().isOk())
            .andExpect(model().attribute("platform", "IOS"))
            .andExpect(model().attribute("appDeepLink", "umc://thread/42"))
            .andExpect(content().string(containsString("UMC PRODUCT 더 알아보기")))
            .andExpect(content().string(not(containsString(PLAY_STORE_URL))));
    }

    @Test
    @DisplayName("데스크톱 User-Agent면 DESKTOP으로 판별하고 Play 스토어 버튼을 보여준다")
    void show_desktopUserAgent_rendersDesktopPlatform() throws Exception {
        mockMvc.perform(get("/community/threads/42").header(HttpHeaders.USER_AGENT, DESKTOP_UA))
            .andExpect(status().isOk())
            .andExpect(model().attribute("platform", "DESKTOP"))
            .andExpect(model().attribute("appDeepLink", "umc://thread/42"))
            .andExpect(content().string(containsString(PLAY_STORE_URL)));
    }

    @Test
    @DisplayName("User-Agent가 없으면 UNKNOWN으로 처리한다")
    void show_missingUserAgent_rendersUnknownPlatform() throws Exception {
        mockMvc.perform(get("/community/threads/42"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("platform", "UNKNOWN"));
    }

    @TestConfiguration
    static class TestDeepLinkConfig {

        @Bean
        DeepLinkProperties deepLinkProperties() {
            return new DeepLinkProperties(
                "https://api.university.neordinary.com",
                "https://university.neordinary.com",
                new DeepLinkProperties.Android("com.umc.product", List.of("AA:BB")),
                new DeepLinkProperties.Ios("8B8B4462NV", "com.umc.product", null)
            );
        }
    }
}
