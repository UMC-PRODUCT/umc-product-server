package com.umc.product.project.application.port.in.query.dto.statistics;

/**
 * 프로젝트별 매칭 완료 인원 수.
 */
public record ProjectMatchingCountInfo(
    Long projectId,
    long matchedMemberCount
) {
}
