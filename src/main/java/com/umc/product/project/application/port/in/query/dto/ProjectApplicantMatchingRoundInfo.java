package com.umc.product.project.application.port.in.query.dto;

/**
 * 프로젝트·차수·멤버 ID 조합. 지원통계(applicantMemberId)·매칭통계(memberId) 공용.
 */
public record ProjectApplicantMatchingRoundInfo(Long projectId, Long roundId, Long memberId) {
}
