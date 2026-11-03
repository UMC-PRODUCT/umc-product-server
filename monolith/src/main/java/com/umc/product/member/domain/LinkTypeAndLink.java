package com.umc.product.member.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

public record LinkTypeAndLink(
    MemberProfileLinkType type,
    String link
) {
    private static final Set<String> OPENABLE_SCHEMES = Set.of("http", "https");

    /**
     * 명함에서 열 수 있는 링크인지 판단합니다.
     * <p>
     * http(s) 스킴과 점(.)이 포함된 호스트를 가진 절대 URL만 허용합니다. 클라이언트(Android)의 링크 검증 기준과 같습니다.
     */
    public boolean isOpenable() {
        if (link == null || link.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(link.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            return scheme != null
                && OPENABLE_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))
                && host != null
                && host.contains(".");
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
