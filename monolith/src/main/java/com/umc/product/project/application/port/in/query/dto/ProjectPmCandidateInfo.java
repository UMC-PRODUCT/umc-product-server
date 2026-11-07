package com.umc.product.project.application.port.in.query.dto;

public record ProjectPmCandidateInfo(
    Long memberId, Long challengerId, String name, String nickname, Long schoolId, String schoolName
) {
}
