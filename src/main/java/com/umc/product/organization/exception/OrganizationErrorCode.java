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
    STUDY_GROUP_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0013", "스터디 그룹 멤버는 최소 1명 이상이어야 합니다."),
    STUDY_GROUP_MEMBER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0014", "스터디 그룹을 만들 때 Member ID는 필수입니다."),
    STUDY_GROUP_MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0015", "이미 존재하는 스터디 그룹 멤버입니다."),
    STUDY_GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0016", "스터디 그룹 멤버를 찾을 수 없습니다."),

    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0017", "지부를 찾을 수 없습니다."),
    SCHOOL_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0018", "학교를 찾을 수 없습니다."),
    GISU_IS_ACTIVE_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0019", "활성화된 기수를 찾을 수 없습니다."),
    GISU_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZAITON-0020", "기수를 찾을 수 없습니다."),
    PART_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0021", "파트는 필수입니다."),
    STUDY_GROUP_NAME_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0022", "유효하지 않은 스터디 그룹 이름입니다."),
    STUDY_GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST, "ORGANIZAITON-0023", "스터디 그룹을 찾을 수 없습니다."),
    STUDY_GROUP_CHALLENGER_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0024",
        "스터디 그룹의 리더 또는 멤버로 존재하지 않는 챌린저가 포함되어 있습니다."),
    LEADER_CANNOT_BE_MEMBER(HttpStatus.BAD_REQUEST, "ORGANIZATION-0025", "스터디 그룹의 리더는 멤버가 될 수 없습니다."),
    STUDY_GROUP_MEMBER_DUPLICATED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0026", "스터디 그룹 멤버 ID에 중복이 있습니다."),
    NO_SUCH_CHAPTER_SCHOOL(HttpStatus.NOT_FOUND, "ORGANIZATION-0027", "요청하신 학교와 지부와 일치하는 정보가 없습니다.."),
    GISU_ALREADY_EXISTS(HttpStatus.CONFLICT, "ORGANIZATION-0028", "이미 존재하는 기수입니다."),
    SCHOOL_ALREADY_ASSIGNED_TO_CHAPTER(HttpStatus.CONFLICT, "ORGANIZATION-0029",
        "해당 기수에서 이미 다른 지부에 배정된 학교가 포함되어 있습니다."),
    CHAPTER_NAME_DUPLICATED(HttpStatus.CONFLICT, "ORGANIZATION-0030", "해당 기수에 동일한 이름의 지부가 이미 존재합니다."),
    STUDY_GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORGANIZATION-0031", "스터디 그룹 조회 권한이 없습니다."),
    GISU_HAS_ASSOCIATED_CHAPTERS(HttpStatus.CONFLICT, "ORGANIZATION-0032", "해당 기수에 연결된 지부 또는 학교가 존재하여 삭제할 수 없습니다."),
    STUDY_GROUP_ORGANIZER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0033", "스터디 그룹 운영진은 최소 1명 이상이어야 합니다."),
    STUDY_GROUP_ORGANIZER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0034" , "스터디 그룹 운영진의 StudyGroupOrganizer ID는 필수입니다."),
    STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY(HttpStatus.CONFLICT, "ORGANIZATION-0035", "다른 스터디 그룹과 중복된 멤버가 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
