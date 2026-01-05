package com.umc.product.organization.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrganizationErrorCode implements BaseCode {

    GISU_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0001", "기수는 필수입니다."),
    ORGAN_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0002", "조직 이름 설정은 필수입니다."),
    SCHOOL_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0003", "학교는 필수입니다."),
    CHAPTER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0004", "지부는 필수입니다."),


    GISU_START_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0005", "기수 시작일은 필수입니다."),
    GISU_END_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0006", "기수 종료일은 필수입니다."),
    GISU_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0007", "기수 시작일은 종료일보다 이전이어야 합니다."),

    SCHOOL_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0008", "학교 이름은 필수입니다."),
    SCHOOL_DOMAIN_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0009", "학교 이메일 도메인은 필수입니다."),

    STUDY_GROUP_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0010", "스터디 그룹 이름은 필수입니다."),
    STUDY_GROUP_LEADER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0011", "스터디 그룹 리더는 필수입니다."),

    STUDY_GROUP_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0012", "스터디 그룹은 필수입니다."),
    STUDY_GROUP_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0013", "스터디 그룹 멤버는 필수입니다."),
    CHALLENGER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0014", "챌린저 ID는 필수입니다."),
    STUDY_GROUP_MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0015", "이미 존재하는 스터디 그룹 멤버입니다."),
    STUDY_GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0016", "스터디 그룹 멤버를 찾을 수 없습니다.");;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
