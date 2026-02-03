package com.umc.product.global.constant;

import io.swagger.v3.oas.models.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SwaggerTag.Constants를 Tag 어노테이션의 name 부분에 집어넣어주시면 됩니다.
 * <p>
 * Order는 Swagger UI 상에 표시되는 순서로, 도메인 간에 10 간격을 주고 넣었으므로 도메인 내부에서 분할하고자 하는 경우 Order를 알맞게 추가해주세요.
 * <p>
 * 추후 RestDocs로 변경하면서 Swagger는 천천히 deprecate 시킬 예정입니다.
 */
@Getter
@RequiredArgsConstructor
public enum SwaggerTag {

    TEST("개발자용", "temp | 개발자용 Test API", 0),
    AUTH("인증/인가", "authentication | 인증/인가 API", 10),
    MEMBER("회원", "member | 회원 API", 20),
    CHALLENGER("챌린저", "challenger | 챌린저 API", 30),
    ORGANIZATION("조직 (학교/지부/중앙운영사무국)", "organization | 조직 관련 API", 40),
    CURRICULUM("커리큘럼", "curriculum | 커리큘럼 관련 API", 50),
    SCHEDULE("행사 및 스터디 일정", "schedule | 행사 및 스터디 관련 API", 60),
    COMMUNITY("커뮤니티", "community | 커뮤니티 API", 70),
    NOTICE("공지사항", "notice | 공지사항 API", 80),
    SURVEY("설문", "survey | 설문 API", 90),
    RECRUITMENT("모집", "recruitment | 모집 API", 100),
    DOC_EVALUATION("서류 평가", "document evaluation | 평가 API", 105),
    INTERVIEW_SCHEDULING("면접 스케줄링 관리", "interview scheduling | 면접 스케줄링 관리 API", 107),
    INTERVIEW_QUESTION_SHEET("면접 질문지 관리", "interview question sheet | 면접 질문지 관리 API", 108),
    INTERVIEW_EVALUATION("면접 평가", "interview evaluation | 면접 평가 API", 109),
    FINAL_SELECTION("최종 합격 관리", "final selection | 최종 합격 관리 API", 110),
    RECRUITMENT_ADMIN("총괄 권한 모집 관리", "recruitment admin | 총괄 권한 모집 API", 111),
    NOTIFICATION("알람", "notification | 알림 API", 110),
    STORAGE("파일 저장소", "storage | 파일 저장 API", 120),
    PROJECT("UPMS", "project | 프로젝트 매칭 API", 130),
    TERMS("약관", "terms | 약관 API", 140),

    // 관리자 전용 API는 최하단에 배치하도록 함
    ADMIN("관리자/운영진", "관리자 전용 별도 API", 999),

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
        public static final String MEMBER = "회원";
        public static final String CHALLENGER = "챌린저";
        public static final String ORGANIZATION = "조직 (학교/지부/중앙운영사무국)";
        public static final String CURRICULUM = "커리큘럼";
        public static final String SCHEDULE = "행사 및 스터디 일정";
        public static final String COMMUNITY = "커뮤니티";
        public static final String NOTICE = "공지사항";
        public static final String SURVEY = "설문";
        public static final String RECRUITMENT = "모집";
        public static final String DOC_EVALUATION = "서류 평가";
        public static final String INTERVIEW_SCHEDULING = "면접 스케줄링 관리";
        public static final String INTERVIEW_QUESTION_SHEET = "면접 질문지 관리";
        public static final String INTERVIEW_EVALUATION = "면접 평가";
        public static final String FINAL_SELECTION = "최종 합격 관리";
        public static final String RECRUITMENT_ADMIN = "총괄 권한 모집 관리";
        public static final String NOTIFICATION = "알람";
        public static final String STORAGE = "파일 저장소";
        public static final String PROJECT = "UPMS";
        public static final String ADMIN = "관리자/운영진";
        public static final String TERMS = "약관";
    }
}
