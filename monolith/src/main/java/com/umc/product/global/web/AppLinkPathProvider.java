package com.umc.product.global.web;

import java.util.List;

/**
 * 앱이 Universal Link로 가로챌 웹 경로를 도메인이 직접 등록하는 확장점(#1268).
 *
 * <p>링크 경로는 각 도메인이 소유하고, {@link WellKnownController}는 등록된 경로를 모아
 * apple-app-site-association의 {@code paths}로만 내보낸다. 새 도메인이 공유 링크를 추가할 때
 * global 코드를 고치지 않아도 된다.</p>
 */
public interface AppLinkPathProvider {

    /** AASA {@code paths} 형식의 경로 패턴(예: {@code /community/threads/*}). */
    List<String> appLinkPaths();
}
