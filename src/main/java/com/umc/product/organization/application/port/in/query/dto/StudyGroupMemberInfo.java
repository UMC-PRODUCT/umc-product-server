package com.umc.product.organization.application.port.in.query.dto;

/**
 * 스터디 그룹 소속 스터디원 1명의 요약 정보.
 * <p>
 * Repository 단계에서는 {@code profileImageUrl} 자리에 storage 파일 ID가 담기고,
 * Service 단계에서 실제 접근 URL로 치환된다 (도메인 경계: Repository는 storage를 모름).
 *
 * @param memberId        멤버 ID
 * @param schoolName      멤버 소속 학교명
 * @param profileImageUrl 멤버 프로필 이미지 URL (Service 치환 전에는 storage 파일 ID)
 */
public record StudyGroupMemberInfo(
        Long memberId,
        String schoolName,
        String profileImageUrl
) {
}
