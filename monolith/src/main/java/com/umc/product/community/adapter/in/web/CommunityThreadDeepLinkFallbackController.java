package com.umc.product.community.adapter.in.web;

import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.umc.product.global.config.DeepLinkProperties;
import com.umc.product.global.security.annotation.Public;

import lombok.RequiredArgsConstructor;

/**
 * 커뮤니티 스레드 공유 링크가 앱이 가로채지 못하고 브라우저로 열렸을 때 보여줄 페이지(#1268).
 *
 * <p>App Link/Universal Link 검증을 통과한 기기에서는 이 요청 자체가 OS 레벨에서 가로채져
 * 서버까지 오지 않는다. 여기까지 도달했다는 건 앱이 없거나 검증 전이라는 뜻이라, "앱에서 열기"
 * 시도와 스토어 안내를 함께 보여준다. 단순 리다이렉트 대신 페이지를 쓰는 건, 리다이렉트는 스토어로
 * 바로 튕겨서 "앱에서 열기"를 다시 시도할 여지가 없기 때문이다.</p>
 */
@Controller
@RequiredArgsConstructor
public class CommunityThreadDeepLinkFallbackController {

    private static final String ANDROID_MARKER = "android";
    private static final String IOS_MARKER_IPHONE = "iphone";
    private static final String IOS_MARKER_IPAD = "ipad";

    private final DeepLinkProperties deepLinkProperties;

    @GetMapping("/community/threads/{threadId}")
    @Public
    public String show(
        @PathVariable Long threadId,
        @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
        Model model
    ) {
        Platform platform = Platform.fromUserAgent(userAgent);
        DeepLinkProperties.Android android = deepLinkProperties.android();
        DeepLinkProperties.Ios ios = deepLinkProperties.ios();

        model.addAttribute("threadId", threadId);
        model.addAttribute("appDeepLink", "umc://thread/" + threadId);
        model.addAttribute("platform", platform.name());
        model.addAttribute("androidStoreUrl", android.playStoreUrl());
        model.addAttribute("iosStoreUrl", blankToNull(ios.appStoreUrl()));
        model.addAttribute("fallbackUrl", deepLinkProperties.fallbackUrl());
        return "community/thread-deep-link";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private enum Platform {
        ANDROID,
        IOS,
        UNKNOWN;

        static Platform fromUserAgent(String userAgent) {
            if (userAgent == null) {
                return UNKNOWN;
            }
            String lower = userAgent.toLowerCase(Locale.ROOT);
            if (lower.contains(ANDROID_MARKER)) {
                return ANDROID;
            }
            if (lower.contains(IOS_MARKER_IPHONE) || lower.contains(IOS_MARKER_IPAD)) {
                return IOS;
            }
            return UNKNOWN;
        }
    }
}
