package com.umc.product.authorization.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 권한 체크 대상이 되는 리소스 타입
 */
@Getter
@RequiredArgsConstructor
public enum ResourceType {

    CURRICULUM("curriculum", "커리큘럼"),
    SCHEDULE("schedule", "일정"),
    NOTICE("notice", "공지사항"),
    COMMUNITY("community", "커뮤니티"),
    FORM("form", "지원서"),
    ORGANIZATION("organization", "기수/지부/학교"),
    MEMBER("member", "사용자"),
    ;

    private final String code;
    private final String description;

    /**
     * code로 ResourceType 찾기
     */
    public static ResourceType fromCode(String code) {
        for (ResourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown resource type: " + code);
    }
}
