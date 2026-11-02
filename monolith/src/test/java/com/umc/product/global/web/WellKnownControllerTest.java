package com.umc.product.global.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.global.config.DeepLinkProperties;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;

@WebMvcTest(controllers = WellKnownController.class)
@Import({JacksonConfig.class, WellKnownControllerTest.TestDeepLinkConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("App Link/Universal Link 검증 파일 서빙")
class WellKnownControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("assetlinks.json은 application/json으로, 설정된 패키지/인증서를 그대로 내려준다")
    void assetLinks_returnsConfiguredFingerprints() throws Exception {
        mockMvc.perform(get("/.well-known/assetlinks.json"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].relation[0]").value("delegate_permission/common.handle_all_urls"))
            .andExpect(jsonPath("$[0].target.namespace").value("android_app"))
            .andExpect(jsonPath("$[0].target.package_name").value("com.umc.product"))
            .andExpect(jsonPath("$[0].target.sha256_cert_fingerprints[0]").value("AA:BB"))
            .andExpect(jsonPath("$[0].target.sha256_cert_fingerprints[1]").value("CC:DD"));
    }

    @Test
    @DisplayName("apple-app-site-association은 application/json으로, TeamID.BundleID와 경로 패턴을 내려준다")
    void appleAppSiteAssociation_returnsConfiguredAppId() throws Exception {
        mockMvc.perform(get("/.well-known/apple-app-site-association"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.applinks.details[0].appID").value("8B8B4462NV.com.umc.product"))
            .andExpect(jsonPath("$.applinks.details[0].paths[0]").value("/community/threads/*"));
    }

    @TestConfiguration
    static class TestDeepLinkConfig {

        @Bean
        DeepLinkProperties deepLinkProperties() {
            return new DeepLinkProperties(
                "https://api.university.neordinary.com",
                null,
                new DeepLinkProperties.Android("com.umc.product", List.of("AA:BB", "CC:DD")),
                new DeepLinkProperties.Ios("8B8B4462NV", "com.umc.product", null)
            );
        }
    }
}
