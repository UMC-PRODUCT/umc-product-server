package com.umc.product.test.application.port.in.command.dto;

/**
 * 프로젝트 시딩 Command. ADR-017 참조.
 *
 * @param projectCount 생성할 프로젝트 수
 * @param gisuId       대상 기수 (null 이면 활성 기수)
 */
public record SeedProjectsCommand(
    int projectCount,
    Long gisuId
) {
}
