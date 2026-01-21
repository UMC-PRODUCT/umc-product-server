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
    STUDY_GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0016", "스터디 그룹 멤버를 찾을 수 없습니다."),

    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0017", "지부를 찾을 수 없습니다."),
    SCHOOL_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0018", "학교를 찾을 수 없습니다."),
    GISU_IS_ACTIVE_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0019", "활성화된 기수를 찾을 수 없습니다."),
    GISU_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0020", "기수를 찾을 수 없습니다."),
    PART_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0021", "파트는 필수입니다."),
    STUDY_GROUP_NAME_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0022", "유효하지 않은 스터디 그룹 이름입니다."),
    STUDY_GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0023", "스터디 그룹을 찾을 수 없습니다."),
    STUDY_GROUP_CHALLENGER_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0024", "스터디 그룹의 리더 또는 멤버로 존재하지 않는 챌린저가 포함되어 있습니다."),
    LEADER_CANNOT_BE_MEMBER(HttpStatus.BAD_REQUEST, "ORGANIZATION-0025", "스터디 그룹의 리더는 멤버가 될 수 없습니다."),
    STUDY_GROUP_MEMBER_DUPLICATED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0026", "스터디 그룹 멤버 ID에 중복이 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
