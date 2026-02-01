package com.umc.product.common.domain.enums;

/**
 * 챌린저가 가질 수 있는 Role의 유형을 정의합니다.
 * <p>
 * Role에 대한 상속 관계는 Spring Security로 표현됩니다.
 * <p>
 * 역할 계층 구조: - SUPER_ADMIN - 중앙운영사무국 (isCentralMember) - 중앙운영사무국 총괄단 (isCentralCore): 총괄, 부총괄 - 중앙운영사무국 운영국원 - 중앙운영사무국
 * 교육국원 - 지부장 - 학교 (isSchoolAdmin) - 학교 회장단 (isSchoolCore): 회장, 부회장 - 교내 파트장 - 기타 교내 운영진 - 챌린저 - 회원/사용자
 */
public enum ChallengerRoleType {
    // 최고 관리자
    SUPER_ADMIN, // 슈퍼 관리자

    // 중앙운영사무국 (isCentralMember)
    CENTRAL_PRESIDENT, // 총괄 (isCentralCore)
    CENTRAL_VICE_PRESIDENT, // 부총괄 (isCentralCore)
    CENTRAL_OPERATING_TEAM_MEMBER, // 중앙운영사무국 운영국원
    CENTRAL_EDUCATION_TEAM_MEMBER, // 중앙운영사무국 교육국원

    // 지부
    CHAPTER_PRESIDENT, // 지부장

    // 학교 (isSchoolAdmin)
    SCHOOL_PRESIDENT, // 회장 (isSchoolCore)
    SCHOOL_VICE_PRESIDENT, // 부회장 (isSchoolCore)
    SCHOOL_PART_LEADER, // 교내 파트장
    SCHOOL_ETC_ADMIN, // 기타 교내 운영진

    // 일반
    CHALLENGER, // 챌린저
    ;

    /**
     * 중앙운영사무국 총괄단 여부를 확인합니다. 총괄, 부총괄이 해당됩니다.
     */
    public boolean isCentralCore() {
        return this == CENTRAL_PRESIDENT || this == CENTRAL_VICE_PRESIDENT;
    }

    /**
     * 중앙운영사무국 멤버 여부를 확인합니다. 총괄단, 운영국원, 교육국원이 해당됩니다.
     */
    public boolean isCentralMember() {
        return isCentralCore()
            || this == CENTRAL_OPERATING_TEAM_MEMBER
            || this == CENTRAL_EDUCATION_TEAM_MEMBER;
    }

    /**
     * 학교 회장단 여부를 확인합니다. 회장, 부회장이 해당됩니다.
     */
    public boolean isSchoolCore() {
        return this == SCHOOL_PRESIDENT || this == SCHOOL_VICE_PRESIDENT;
    }

    /**
     * 학교 관리자 여부를 확인합니다. 회장단, 파트장, 기타 운영진이 해당됩니다.
     */
    public boolean isSchoolAdmin() {
        return isSchoolCore()
            || this == SCHOOL_PART_LEADER
            || this == SCHOOL_ETC_ADMIN;
    }

    /**
     * 해당 역할이 속하는 조직 타입을 반환합니다.
     */
    public OrganizationType organizationType() {
        if (this == CHAPTER_PRESIDENT) {
            return OrganizationType.CHAPTER;
        }
        if (isSchoolAdmin()) {
            return OrganizationType.SCHOOL;
        }
        return OrganizationType.CENTRAL;
    }
}
