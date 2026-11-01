package com.umc.product.community.domain.enums;

public enum CommunityThreadMemberState {
    ACTIVE,
    LEFT,
    /** 강퇴됐지만 아직 확인(acknowledge)하지 않은 상태. 목록에는 노출되지만 비활성으로 표시된다. */
    KICKED,
    /** 강퇴 사실을 확인한 상태. 이후로는 목록 조회에서 완전히 제외된다. */
    KICKED_ACKNOWLEDGED
}
