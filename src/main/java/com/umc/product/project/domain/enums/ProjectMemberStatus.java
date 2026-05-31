package com.umc.product.project.domain.enums;

public enum ProjectMemberStatus {
    ACTIVE, // 현재 활동 중
    COMPLETED, // 정상적으로 활동을 마친 경우
    WITHDRAWN, // 프로젝트가 속한 기수가 종료되기 전에 팀에서 사정이 있어서 나간 경우.
    DISMISSED // 프로젝트에서 강제로 퇴출된 경우
}
