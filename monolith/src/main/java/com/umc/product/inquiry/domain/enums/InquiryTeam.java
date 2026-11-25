package com.umc.product.inquiry.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 문의를 받는 팀. 질문자가 고르는 화면의 이름과 담당 범위 설명을 함께 둔다. 팀을 모르면 null로 저장한다.
 */
@Getter
@RequiredArgsConstructor
public enum InquiryTeam {
    BRAND_GROWTH("Brand & Growth", "모집·선발, 공식 홈페이지, 테크 블로그"),
    LEARNING_ACTIVITY("Learning & Activity", "워크북·미션, 활동 참여, 출석·수료, UPMS"),
    COMMUNITY_CONNECTION("Community & Connection", "커뮤니티·Connect, 프로필·전자명함, 채팅 이용"),
    OPERATIONS_ENABLEMENT("Operations Enablement", "기수·조직·역할 관리, 공지, 출석·운영 승인"),
    PLATFORM("Platform", "로그인·인증, 알림·채팅 전달 오류, 공통 폼"),
    DESIGN_PLATFORM("Design Platform", "디자인 시스템, Figma 라이브러리·토큰"),
    INFRASTRUCTURE("Infrastructure", "배포·실행 환경, 장애·모니터링");

    public static final String UNKNOWN_DISPLAY_NAME = "어느 팀인지 모르겠어요";
    public static final String UNKNOWN_DESCRIPTION = "UMC PRODUCT에서 담당 팀을 확인해요";

    private final String displayName;
    private final String description;
}
