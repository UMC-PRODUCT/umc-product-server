package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/**
 * Notice 시딩 Command. ADR-017 참조.
 *
 * @param gisuId          대상 기수 (null 이면 활성 기수)
 * @param authorMemberId  공지 작성자 멤버 ID. {@link com.umc.product.notice.domain.enums.NoticeTargetPattern}
 *                        별 권한 검증을 통과해야 한다. 단일 멤버로 모든 범위를 커버하려면 중앙 총괄단
 *                        ({@code isCentralCore}) 권한을 가진 멤버 ID 를 권장한다.
 * @param globalCount     전체 기수 대상 공지 수 (SPECIFIC_GISU_ALL_TARGET)
 * @param perChapterCount 각 지부당 생성할 공지 수 (SPECIFIC_GISU_SPECIFIC_CHAPTER)
 * @param perSchoolCount  각 학교당 생성할 공지 수 (SPECIFIC_GISU_SPECIFIC_SCHOOL)
 * @param perPartCount    각 파트당 생성할 공지 수 (SPECIFIC_GISU_SPECIFIC_PART). ADMIN 제외.
 * @param parts           시딩할 파트 목록 (null/empty 면 ADMIN 제외 모든 파트)
 */
public record SeedNoticeCommand(
    Long gisuId,
    Long authorMemberId,
    int globalCount,
    int perChapterCount,
    int perSchoolCount,
    int perPartCount,
    List<ChallengerPart> parts
) {
}
