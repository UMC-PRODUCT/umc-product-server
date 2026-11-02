package com.umc.product.community.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Android User-Agent면 Play 스토어 안내를 보여준다")
    void show_androidUserAgent_rendersAndroidStore() throws Exception {
        mockMvc.perform(get("/community/threads/42")
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Linux; Android 14)"))
            .andExpect(status().isOk())
            .andExpect(view().name("community/thread-deep-link"))
            .andExpect(model().attribute("platform", "ANDROID"))
            .andExpect(model().attribute("appDeepLink", "umc://thread/42"))
            .andExpect(model().attribute("androidStoreUrl", "https://play.google.com/store/apps/details?id=com.umc.product"));
    }

    @Test
    @DisplayName("iOS User-Agent면 iOS 플랫폼으로 판별한다")
    void show_iosUserAgent_rendersIosPlatform() throws Exception {
        mockMvc.perform(get("/community/threads/42")
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X)"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("platform", "IOS"));
    }

    @Test
    @DisplayName("User-Agent가 없거나 판별되지 않으면 UNKNOWN으로 처리한다")
    void show_unknownUserAgent_rendersUnknownPlatform() throws Exception {
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
