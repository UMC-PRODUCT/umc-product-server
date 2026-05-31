package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/**
 * Curriculum 시딩 Command. ADR-017 참조.
 *
 * @param gisuId                   대상 기수 (null 이면 활성 기수)
 * @param weeksPerCurriculum       각 커리큘럼 당 생성할 주차 수 (예: 8 이면 1~8 주차)
 * @param missionsPerWorkbook      각 OriginalWorkbook 당 생성할 미션 수 (0 이상)
 * @param parts                    시딩할 파트 목록 (null/empty 면 ADMIN 제외 모든 파트)
 * @param releaseRequesterMemberId 지정 시 생성된 워크북을 READY → RELEASED 로 전환할 때
 *                                 releasedMemberId 로 기록할 멤버 ID. null 이면 READY 상태 유지.
 */
public record SeedCurriculumCommand(
    Long gisuId,
    int weeksPerCurriculum,
    int missionsPerWorkbook,
    List<ChallengerPart> parts,
    Long releaseRequesterMemberId
) {
}
