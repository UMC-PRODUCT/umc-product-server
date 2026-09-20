package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * Curriculum 시딩 결과. ADR-017 참조.
 *
 * @param gisuId                         시딩 대상 기수
 * @param createdCurriculumIds           생성된 Curriculum ID 목록
 * @param createdWeeklyCurriculumIds     생성된 WeeklyCurriculum ID 목록
 * @param createdOriginalWorkbookIds     생성된 OriginalWorkbook ID 목록
 * @param createdMissionIds              생성된 OriginalWorkbookMission ID 목록
 * @param released                       워크북을 RELEASED 까지 전환한 경우 true
 * @param curriculumFailed               Curriculum 생성 단계 실패 수
 * @param weeklyCurriculumFailed         WeeklyCurriculum 생성 단계 실패 수
 * @param originalWorkbookFailed         OriginalWorkbook 생성 단계 실패 수
 * @param missionFailed                  OriginalWorkbookMission 생성 단계 실패 수
 * @param releaseFailed                  RELEASED 전환 단계 실패 수 (released=false 면 항상 0)
 */
public record SeedCurriculumResult(
    Long gisuId,
    List<Long> createdCurriculumIds,
    List<Long> createdWeeklyCurriculumIds,
    List<Long> createdOriginalWorkbookIds,
    List<Long> createdMissionIds,
    boolean released,
    int curriculumFailed,
    int weeklyCurriculumFailed,
    int originalWorkbookFailed,
    int missionFailed,
    int releaseFailed
) {
}
