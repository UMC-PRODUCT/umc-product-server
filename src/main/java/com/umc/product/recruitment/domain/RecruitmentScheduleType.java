package com.umc.product.recruitment.domain;

public enum RecruitmentScheduleType {
    APPLY_WINDOW(1, "서류 모집", Kind.WINDOW),
    DOC_REVIEW_WINDOW(2, "서류 평가", Kind.WINDOW),
    DOC_RESULT_AT(3, "서류 결과 발표", Kind.AT),
    INTERVIEW_WINDOW(4, "면접 진행", Kind.WINDOW),
    FINAL_REVIEW_WINDOW(5, "최종 평가", Kind.WINDOW),
    FINAL_RESULT_AT(6, "최종 결과 발표", Kind.AT),
    OT_AT(7, "OT", Kind.AT),
    ACTIVITY_WINDOW(8, "활동 기간", Kind.WINDOW);

    public enum Kind {WINDOW, AT}

    private final int stepOrder;
    private final String title;
    private final Kind kind;

    RecruitmentScheduleType(int stepOrder, String title, Kind kind) {
        this.stepOrder = stepOrder;
        this.title = title;
        this.kind = kind;
    }

    public int stepOrder() {
        return stepOrder;
    }

    public String title() {
        return title;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isWindow() {
        return kind == Kind.WINDOW;
    }

    public boolean isAt() {
        return kind == Kind.AT;
    }

}
