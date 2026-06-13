package com.umc.product.organization.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrganizationErrorCode implements BaseCode {

    GISU_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0001", "기수를 선택해주세요."),
    ORGAN_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0002", "조직 이름을 입력해주세요."),
    SCHOOL_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0003", "학교를 선택해주세요."),
    CHAPTER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0004", "지부를 선택해주세요."),


    GISU_START_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0005", "기수 시작일을 선택해주세요."),
    GISU_END_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0006", "기수 종료일을 선택해주세요."),
    GISU_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0007", "기수 시작일은 종료일보다 빨라야 해요. 기간을 다시 선택해주세요."),

    SCHOOL_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0008", "학교 이름을 입력해주세요."),
    SCHOOL_DOMAIN_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0009", "학교 이메일 도메인을 입력해주세요."),

    STUDY_GROUP_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0010", "스터디 그룹 이름을 입력해주세요."),
    STUDY_GROUP_LEADER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0011", "스터디 그룹 리더를 선택해주세요."),

    STUDY_GROUP_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0012", "스터디 그룹을 선택해주세요."),
    STUDY_GROUP_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0013", "스터디 그룹 멤버는 1명 이상 선택해주세요."),
    STUDY_GROUP_MEMBER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0014", "스터디 그룹 멤버를 선택해주세요."),
    STUDY_GROUP_MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ORGANIZATION-0015", "이미 스터디 그룹에 포함된 멤버예요. 멤버 목록을 확인해주세요."),
    STUDY_GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0016", "스터디 그룹 멤버를 찾을 수 없어요. 멤버 목록을 확인해주세요."),

    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0017", "지부를 찾을 수 없어요. 선택한 지부를 확인해주세요."),
    SCHOOL_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0018", "학교를 찾을 수 없어요. 선택한 학교를 확인해주세요."),
    GISU_IS_ACTIVE_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0019", "활성화된 기수를 찾을 수 없어요. 기수 설정을 확인해주세요."),
    GISU_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0020", "기수를 찾을 수 없어요. 선택한 기수를 확인해주세요."),
    PART_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0021", "파트를 선택해주세요."),
    STUDY_GROUP_NAME_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0022", "스터디 그룹 이름이 올바르지 않아요. 이름을 확인해주세요."),
    STUDY_GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST, "ORGANIZATION-0023", "스터디 그룹을 찾을 수 없어요. 선택한 그룹을 확인해주세요."),

    STUDY_GROUP_CHALLENGER_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0024",
        "스터디 그룹 리더 또는 멤버에 존재하지 않는 챌린저가 있어요. 구성원을 확인해주세요."),
    LEADER_CANNOT_BE_MEMBER(HttpStatus.BAD_REQUEST, "ORGANIZATION-0025", "스터디 그룹 리더는 멤버로 중복 등록할 수 없어요. 구성원을 확인해주세요."),
    STUDY_GROUP_MEMBER_DUPLICATED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0026", "스터디 그룹 멤버가 중복됐어요. 멤버 목록을 확인해주세요."),
    NO_SUCH_CHAPTER_SCHOOL(HttpStatus.NOT_FOUND, "ORGANIZATION-0027", "학교와 지부 연결 정보를 찾을 수 없어요. 배정 정보를 확인해주세요."),
    GISU_ALREADY_EXISTS(HttpStatus.CONFLICT, "ORGANIZATION-0028", "이미 존재하는 기수예요. 기존 기수를 확인해주세요."),
    SCHOOL_ALREADY_ASSIGNED_TO_CHAPTER(HttpStatus.CONFLICT, "ORGANIZATION-0029",
        "해당 기수에서 이미 다른 지부에 배정된 학교가 있어요. 학교 배정 정보를 확인해주세요."),
    CHAPTER_NAME_DUPLICATED(HttpStatus.CONFLICT, "ORGANIZATION-0030", "해당 기수에 같은 이름의 지부가 이미 있어요. 다른 이름을 입력해주세요."),

    STUDY_GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORGANIZATION-0031",
        "스터디 그룹을 조회할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."),
    GISU_HAS_ASSOCIATED_CHAPTERS(HttpStatus.CONFLICT, "ORGANIZATION-0032", "연결된 지부 또는 학교가 있어 기수를 삭제할 수 없어요. 연결 정보를 먼저 정리해주세요."),

    STUDY_GROUP_MENTOR_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0033", "스터디 그룹 파트장은 1명 이상 선택해주세요."),
    STUDY_GROUP_MENTOR_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0034",
        "스터디 그룹 파트장을 선택해주세요."),
    STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY(HttpStatus.CONFLICT, "ORGANIZATION-0035", "다른 스터디 그룹에 이미 속한 멤버가 있어요. 멤버 목록을 확인해주세요."),
    STUDY_GROUP_MENTOR_DUPLICATED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0036", "이미 해당 스터디에 속한 파트장이에요. 파트장 목록을 확인해주세요."),
    STUDY_GROUP_MENTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0037", "스터디 그룹 파트장 정보를 찾을 수 없어요. 파트장 목록을 확인해주세요."),

    STUDY_GROUP_SCHEDULE_ATTENDANCE_POLICY_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0038",
        "스터디 그룹 일정에는 출석 정책이 필요해요. 출석 정책을 설정해주세요."),

    UMC_PRODUCT_GENERATION_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0039", "UMC Product 기수는 필수입니다."),
    UMC_PRODUCT_GENERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0040", "UMC Product 기수를 찾을 수 없습니다."),
    UMC_PRODUCT_GENERATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "ORGANIZATION-0041", "이미 존재하는 UMC Product 기수입니다."),
    UMC_PRODUCT_GENERATION_START_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0042",
        "UMC Product 기수 시작일은 필수입니다."),
    UMC_PRODUCT_GENERATION_END_AT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0043",
        "UMC Product 기수 종료일은 필수입니다."),
    UMC_PRODUCT_GENERATION_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0044",
        "UMC Product 기수 시작일은 종료일보다 이전이어야 합니다."),
    UMC_PRODUCT_MEMBER_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0045", "UMC Product 인원은 필수입니다."),
    UMC_PRODUCT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0046", "UMC Product 인원을 찾을 수 없습니다."),
    UMC_PRODUCT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "ORGANIZATION-0047", "이미 등록된 UMC Product 인원입니다."),
    UMC_PRODUCT_MEMBER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0048", "회원 ID는 필수입니다."),
    UMC_PRODUCT_FUNCTIONAL_MEMBERSHIP_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0049",
        "UMC Product 기능 조직 활동 기록은 필수입니다."),
    UMC_PRODUCT_FUNCTIONAL_UNIT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0050",
        "UMC Product 기능 조직은 필수입니다."),
    UMC_PRODUCT_ROLE_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0051", "UMC Product 직책은 필수입니다."),
    UMC_PRODUCT_POSITION_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0052", "UMC Product 포지션은 필수입니다."),
    UMC_PRODUCT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORGANIZATION-0053", "UMC Product 관리 권한이 없습니다."),
    UMC_PRODUCT_FUNCTIONAL_UNIT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0054",
        "UMC Product 기능 조직을 찾을 수 없습니다."),
    UMC_PRODUCT_FUNCTIONAL_UNIT_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0055",
        "UMC Product 기능 조직 유형은 필수입니다."),
    UMC_PRODUCT_FUNCTIONAL_UNIT_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0056",
        "UMC Product 기능 조직 코드는 필수입니다."),
    UMC_PRODUCT_FUNCTIONAL_UNIT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0057",
        "UMC Product 기능 조직 이름은 필수입니다."),
    UMC_PRODUCT_SQUAD_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0058", "UMC Product Squad는 필수입니다."),
    UMC_PRODUCT_SQUAD_NOT_FOUND(HttpStatus.NOT_FOUND, "ORGANIZATION-0059", "UMC Product Squad를 찾을 수 없습니다."),
    UMC_PRODUCT_SQUAD_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0060", "UMC Product Squad 코드는 필수입니다."),
    UMC_PRODUCT_SQUAD_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0061", "UMC Product Squad 이름은 필수입니다."),
    UMC_PRODUCT_SQUAD_PERIOD_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0062",
        "UMC Product Squad 시작일은 종료일보다 이전이어야 합니다."),
    UMC_PRODUCT_SQUAD_PARTICIPANT_REQUIRED(HttpStatus.BAD_REQUEST, "ORGANIZATION-0063",
        "UMC Product Squad 참여자는 필수입니다."),
    UMC_PRODUCT_FUNCTIONAL_UNIT_PARENT_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0064",
        "UMC Product 기능 조직은 자기 자신을 상위 조직으로 지정할 수 없습니다."),
    GISU_QUERY_CONDITION_INVALID(HttpStatus.BAD_REQUEST, "ORGANIZATION-0065", "기수 조회 조건이 올바르지 않습니다."),

    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
