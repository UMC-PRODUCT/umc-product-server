package com.umc.product.community.domain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 커뮤니티 스레드의 공유 웹 경로와 앱 커스텀 스킴을 한 곳에서 정의한다(#1268).
 *
 * <p>공유 URL, fallback 페이지 매핑, AASA {@code paths}, 점검 예외 경로, 푸시 딥링크가 모두 이 값을
 * 기준으로 하므로, 경로를 바꿀 때는 이 클래스만 고친다.</p>
 */
public final class CommunityThreadLinks {

    public static final String WEB_PATH_PREFIX = "/community/threads/";
    public static final String WEB_PATH_TEMPLATE = WEB_PATH_PREFIX + "{threadId}";
    public static final String APP_LINK_PATH_PATTERN = WEB_PATH_PREFIX + "*";

    // 모바일 양 앱이 이미 처리하는 커스텀 스킴이다 (iOS MessageLink, Android 쪽은 별도 티켓에서 라우팅 추가 예정).
    private static final String APP_SCHEME = "umc";
    private static final String APP_HOST = "thread";

    private CommunityThreadLinks() {
    }

    /** {@code /community/threads/{threadId}} 웹 경로. */
    public static String webPath(Long threadId) {
        return WEB_PATH_PREFIX + threadId;
    }

    /** {@code umc://thread/{threadId}} 앱 커스텀 스킴 URL. */
    public static String appUrl(Long threadId) {
        return APP_SCHEME + "://" + APP_HOST + "/" + threadId;
    }

    /**
     * Android Chrome·인앱 브라우저용 intent URL. 앱이 있으면 {@link #appUrl(Long)}과 같은 화면을 열고,
     * 없으면 {@code storeUrl}(Play 스토어)로 이동한다. 커스텀 스킴 링크는 앱이 없을 때 아무 반응이 없어서 쓴다.
     */
    public static String androidIntentUrl(Long threadId, String packageName, String storeUrl) {
        return "intent://" + APP_HOST + "/" + threadId
            + "#Intent;scheme=" + APP_SCHEME
            + ";package=" + packageName
            + ";S.browser_fallback_url=" + URLEncoder.encode(storeUrl, StandardCharsets.UTF_8)
            + ";end";
    }
}
