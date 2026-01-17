package com.umc.product.global.constant;

import io.swagger.v3.oas.models.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SwaggerTag {

    TEST("개발자용", "temp | 개발자용 Test API", 0),
    AUTH("인증/인가", "authentication | 인증/인가 API", 1),
    MEMBER("회원", "member | 회원 API", 2),
    CHALLENGER("챌린저", "challenger | 챌린저 API", 3),
    ORGANIZATION("조직 (학교/지부/중앙운영사무국)", "organization | 조직 관련 API", 4),
    CURRICULUM("커리큘럼", "curriculum | 커리큘럼 관련 API", 5),
    SCHEDULE("행사 및 스터디 일정", "schedule | 행사 및 스터디 관련 API", 6),
    COMMUNITY("커뮤니티", "community | 커뮤니티 API", 7),
    NOTICE("공지사항", "notice | 공지사항 API", 8),
    SURVEY("설문", "survey | 설문 API", 9),
    RECRUITMENT("모집", "recruitment | 모집 API", 10),
    NOTIFICATION("알람", "notification | 알림 API", 11),
    STORAGE("파일 저장소", "storage | 파일 저장 API", 12),
    PROJECT("UPMS", "project | 프로젝트 매칭 API", 13),


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
        public static final String TEST = SwaggerTag.TEST.getName();
        public static final String AUTH = SwaggerTag.AUTH.getName();
        public static final String MEMBER = SwaggerTag.MEMBER.getName();
        public static final String CHALLENGER = SwaggerTag.CHALLENGER.getName();
        public static final String ORGANIZATION = SwaggerTag.ORGANIZATION.getName();
        public static final String CURRICULUM = SwaggerTag.CURRICULUM.getName();
        public static final String SCHEDULE = SwaggerTag.SCHEDULE.getName();
        public static final String COMMUNITY = SwaggerTag.COMMUNITY.getName();
        public static final String NOTICE = SwaggerTag.NOTICE.getName();
        public static final String SURVEY = SwaggerTag.SURVEY.getName();
        public static final String RECRUITMENT = SwaggerTag.RECRUITMENT.getName();
        public static final String NOTIFICATION = SwaggerTag.NOTIFICATION.getName();
        public static final String STORAGE = SwaggerTag.STORAGE.getName();
        public static final String PROJECT = SwaggerTag.PROJECT.getName();
        public static final String ADMIN = SwaggerTag.ADMIN.getName();
    }
}
