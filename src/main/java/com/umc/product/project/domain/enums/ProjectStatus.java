package com.umc.product.project.domain.enums;

public enum ProjectStatus {
    IN_PROGRESS, // 기수와 무관하게, 프로젝트가 계속 진행 중인 경우
    COMPLETED, // 기수 종료에 따라서 프로젝트를 정상적으로 완료한 경우
    ABORTED, // 프로젝트가 기수 종료 시까지 정상적으로 운영되지 못하고 데모데이 불참 등으로 와해된 경우
}
