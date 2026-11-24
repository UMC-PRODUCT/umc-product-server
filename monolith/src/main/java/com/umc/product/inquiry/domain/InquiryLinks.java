package com.umc.product.inquiry.domain;

/**
 * 문의 푸시 딥링크. 플랫폼 구분 없이 같은 커스텀 스킴을 쓴다.
 */
public final class InquiryLinks {

    private static final String APP_SCHEME = "umc";
    private static final String APP_HOST = "inquiry";

    private InquiryLinks() {
    }

    /** {@code umc://inquiry/{inquiryId}} 앱 커스텀 스킴 URL. */
    public static String appUrl(Long inquiryId) {
        return APP_SCHEME + "://" + APP_HOST + "/" + inquiryId;
    }
}
