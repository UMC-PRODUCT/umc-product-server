package com.umc.product.project.application.port.in.query.dto;

import lombok.Getter;

@Getter
public enum ProjectPermissionReason {
    PROJECT_NOT_FOUND("프로젝트를 찾을 수 없습니다."),
    PERMISSION_DENIED("권한이 없습니다."),
    INVALID_PROJECT_STATUS("현재 상태에서는 이 작업을 수행할 수 없습니다."),
    PROJECT_INFO_REQUIRED("프로젝트 기본 정보가 필요합니다."),
    APPLICATION_FORM_NOT_FOUND("지원 폼이 필요합니다."),
    APPLICATION_FORM_ALREADY_EXISTS("이미 지원 폼이 존재합니다."),
    PART_QUOTA_REQUIRED("파트별 정원이 필요합니다."),
    ACTIVE_MATCHING_ROUND_EXISTS("진행 중인 매칭 차수가 있어 지원 폼을 수정할 수 없습니다."),
    PROJECT_APPLICATION_SELF_APPLY_NOT_ALLOWED("내가 운영하는 프로젝트에는 지원할 수 없습니다."),
    PROJECT_APPLICATION_MEMBER_ALREADY_IN_TEAM("이미 해당 기수에서 팀에 소속되어 있습니다."),
    PROJECT_APPLICATION_PART_NOT_ALLOWED("본인 파트 모집 정원이 존재하지 않습니다."),
    MATCHING_ROUND_NOT_OPEN("지원 가능한 매칭 차수가 열려 있지 않습니다."),
    NOT_PROJECT_GISU_CHALLENGER("해당 프로젝트 기수의 지원 가능한 챌린저가 아닙니다."),
    NOT_IMPLEMENTED("아직 지원하지 않는 기능입니다.");

    private final String message;

    ProjectPermissionReason(String message) {
        this.message = message;
    }
}
