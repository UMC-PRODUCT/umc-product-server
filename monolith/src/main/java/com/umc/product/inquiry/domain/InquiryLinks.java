package com.umc.product.inquiry.domain;

public final class InquiryLinks {

    private static final String APP_SCHEME = "umc";
    private static final String APP_HOST = "inquiry";

    private InquiryLinks() {
    }

    public static String appUrl(Long inquiryId) {
        return APP_SCHEME + "://" + APP_HOST + "/" + inquiryId;
    }
}
