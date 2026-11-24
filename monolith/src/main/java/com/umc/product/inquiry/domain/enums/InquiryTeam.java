package com.umc.product.inquiry.domain.enums;

/**
 * 문의자가 고른 담당 팀. 목록 표시와 필터에만 쓰고 열람 권한에는 쓰지 않는다.
 * <p>
 * 값이 없으면(null) 문의자가 "어느 팀인지 모르겠어요"를 고른 것이다.
 */
public enum InquiryTeam {
    BRAND_GROWTH,
    LEARNING_ACTIVITY,
    COMMUNITY_CONNECTION,
    OPERATIONS_ENABLEMENT,
    PLATFORM,
    DESIGN_PLATFORM,
    INFRASTRUCTURE
}
