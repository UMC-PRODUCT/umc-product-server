package com.umc.product.organization.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrganizationErrorCode implements BaseCode {

    GISU_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-401", "기수는 필수입니다."),
    ORGAN_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-402", "조직 이름 설정은 필수입니다."),
    SCHOOL_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-403", "학교는 필수입니다."),
    CHAPTER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-404", "지부는 필수입니다."),


    GISU_START_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-405", "기수 시작일은 필수입니다."),
    GISU_END_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-406", "기수 종료일은 필수입니다."),
    GISU_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "ORGAN-407", "기수 시작일은 종료일보다 이전이어야 합니다."),

    SCHOOL_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-408", "학교 이름은 필수입니다."),
    SCHOOL_DOMAIN_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-409", "학교 이메일 도메인은 필수입니다."),

    STUDY_GROUP_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-410", "스터디 그룹 이름은 필수입니다."),
    STUDY_GROUP_LEADER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-411", "스터디 그룹 리더는 필수입니다."),

    STUDY_GROUP_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-412", "스터디 그룹은 필수입니다."),
    STUDY_GROUP_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-413", "스터디 그룹 멤버는 필수입니다."),
    CHALLENGER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORGAN-414", "챌린저 ID는 필수입니다."),
    STUDY_GROUP_MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ORGAN-415", "이미 존재하는 스터디 그룹 멤버입니다."),
    STUDY_GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGAN-416", "스터디 그룹 멤버를 찾을 수 없습니다.");;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
