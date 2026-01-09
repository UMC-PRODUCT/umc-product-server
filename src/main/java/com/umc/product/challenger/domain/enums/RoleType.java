package com.umc.product.challenger.domain.enums;

public enum RoleType {
    // 중앙
    CENTRAL_PRESIDENT, // 총괄
    CENTRAL_VICE_PRESIDENT, // 부총괄
    CENTRAL_DIRECTOR, // 국장
    CENTRAL_MANAGER, // 국원
    CENTRAL_PART_LEADER, // 중앙 파트장

    // 지부
    CHAPTER_LEADER, // 지부장
    CHAPTER_STAFF, // 지부 운영진

    // 학교
    SCHOOL_PRESIDENT, // 회장
    SCHOOL_VICE_PRESIDENT, // 부회장
    SCHOOL_PART_LEADER, // 파트장
    SCHOOL_STAFF, // 기타 운영진

    // 일반
    CHALLENGER, // 챌린저
}
