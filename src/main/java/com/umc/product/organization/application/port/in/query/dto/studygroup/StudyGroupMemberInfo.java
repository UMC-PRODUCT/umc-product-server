package com.umc.product.organization.application.port.in.query.dto.studygroup;

/**
 * 스터디원, 파트장 여부와 관계없이 한 명의 정보를 나타내는데 사용됩니다.
 * <p>
 * 스터디 그룹 소속 스터디원 1명의 요약 정보.
 * <p>
 * Repository 단계에서는 {@code profileImageUrl} 자리에 storage 파일 ID가 담기고, Service 단계에서 실제 접근 URL로 치환된다
 * <p>
 * (도메인 경계: Repository는 storage를 모름).
 * <p>
 * Projection과도 연관되어 있으니 변경할 때 유의할 것.
 *
 * @param memberId        멤버 ID
 * @param schoolId        멤버 소속 학교 ID
 * @param schoolName      멤버 소속 학교명
 * @param profileImageUrl 멤버 프로필 이미지 URL (Service 치환 전에는 storage 파일 ID)
 */
public record StudyGroupMemberInfo(
    Long studyGroupId,
    Long memberId,
    String memberName,
    Long schoolId,
    String schoolName,
    String profileImageId,
    String profileImageUrl
) {
    // Projection용
    public StudyGroupMemberInfo(
        Long studyGroupId,
        Long memberId, String memberName,
        Long schoolId, String schoolName,
        String profileImageId
    ) {
        this(
            studyGroupId,
            memberId, memberName,
            schoolId, schoolName,
            profileImageId, null
        );
    }

    public static StudyGroupMemberInfo create(
        Long studyGroupId,
        Long memberId, String memberName,
        Long schoolId, String schoolName,
        String profileImageId, String profileImageUrl
    ) {
        return new StudyGroupMemberInfo(
            studyGroupId,
            memberId, memberName,
            schoolId, schoolName,
            profileImageId, profileImageUrl
        );
    }
}
