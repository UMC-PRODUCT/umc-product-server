package com.umc.product.global.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.config.DeepLinkProperties;
import com.umc.product.global.security.annotation.Public;

import lombok.RequiredArgsConstructor;

/**
 * Android App Link / iOS Universal Link 소유권 검증 파일을 서빙한다(#1268).
 *
 * <p>두 파일 모두 OS가 "이 도메인 링크를 이 앱이 열어도 되는지" 판단할 때 직접 요청하는 정적
 * 자원이라, 리다이렉트 없이 이 경로 그대로 응답해야 한다. AASA는 확장자가 없지만 Apple 요구사항상
 * {@code Content-Type: application/json}으로 응답한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class WellKnownController {

    private static final String RELATION = "delegate_permission/common.handle_all_urls";
    private static final List<String> APP_LINK_PATHS = List.of("/community/threads/*");

    private final DeepLinkProperties deepLinkProperties;

    @GetMapping(value = "/.well-known/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Public
    public ResponseEntity<List<Map<String, Object>>> assetLinks() {
        DeepLinkProperties.Android android = deepLinkProperties.android();
        Map<String, Object> statement = Map.of(
            "relation", List.of(RELATION),
            "target", Map.of(
                "namespace", "android_app",
                "package_name", android.packageName(),
                "sha256_cert_fingerprints", android.sha256CertFingerprints()
            )
        );
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(List.of(statement));
    }

    @GetMapping(value = "/.well-known/apple-app-site-association", produces = MediaType.APPLICATION_JSON_VALUE)
    @Public
    public ResponseEntity<Map<String, Object>> appleAppSiteAssociation() {
        DeepLinkProperties.Ios ios = deepLinkProperties.ios();
        Map<String, Object> detail = Map.of(
            "appID", ios.appId(),
            "paths", APP_LINK_PATHS
        );
        Map<String, Object> applinks = Map.of(
            "apps", List.of(),
            "details", List.of(detail)
        );
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("applinks", applinks));
    }
}
