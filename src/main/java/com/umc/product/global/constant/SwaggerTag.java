package com.umc.product.global.constant;

import io.swagger.v3.oas.models.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SwaggerTag {

    TEST("개발자용", "개발자용 Test API", 0),
    AUTH("인증/인가", "인증/인가 API", 2),
    MEMBER("사용자", "사용자 API", 1),
    CHALLENGER("챌린저", "챌린저 API", 2),
    ORGANIZATION("조직 (학교/지부/중앙운영사무국)", "조직 관련 API", 2),
    CURRICULUM("커리큘럼", "커리큘럼 관련 API", 2),
    SCHEDULE("행사 및 스터디 일정", "행사 및 스터디 관련 API", 2),
    COMMUNITY("커뮤니티", "커뮤니티 API", 2),
    NOTICE("공지사항", "공지사항 API", 2),
    RECRUITMENT("모집/지원", "모집/지원 API", 2),

    ADMIN("관리자/운영진", "관리자 전용 별도 API", 99),

    // 추가하는 경우, 하단의 Constatns에도 반드시 동일하게 추가할 것
    ;

    private final String name;
    private final String description;
    private final int order;  // Swagger UI에서 표시 순서

    /**
     * 이름으로 찾기
     */
    public static SwaggerTag fromName(String name) {
        for (SwaggerTag tag : values()) {
            if (tag.name.equals(name)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown tag: " + name);
    }

    /**
     * OpenAPI Tag 객체로 변환
     */
    public Tag toTag() {
        return new Tag()
                .name(this.name)
                .description(this.description);
    }

    /**
     * Annotation에서 사용할 수 있도록 상수로 정의
     *
     * @Tag(name = SwaggerTag.Constants.USER)
     */
    public static class Constants {
        public static final String TEST = "개발자용";
        public static final String AUTH = "인증/인가";
        public static final String MEMBER = "사용자";
        public static final String CHALLENGER = "챌린저";
        public static final String ORGANIZATION = "조직 (학교/지부/중앙운영사무국)";
        public static final String CURRICULUM = "커리큘럼";
        public static final String SCHEDULE = "행사 및 스터디 일정";
        public static final String COMMUNITY = "커뮤니티";
        public static final String NOTICE = "공지사항";
        public static final String RECRUITMENT = "지원";
        public static final String ADMIN = "관리자/운영진";
    }
}
