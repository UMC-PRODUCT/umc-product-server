package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * 프로젝트 시딩 결과. ADR-017 참조.
 *
 * @param createdProjectIds 모든 멤버 슬롯이 채워진 프로젝트 ID 목록
 * @param partialProjects   프로젝트는 생성되었으나 일부 멤버 슬롯이 채워지지 못한 결과
 * @param skippedChapters   풀 부족으로 시딩이 시도되지 않은 (Chapter, School) 셀 목록
 * @param failedCount       프로젝트 생성 단계 자체가 실패한 횟수
 */
public record SeedProjectsResult(
    List<Long> createdProjectIds,
    List<PartialProject> partialProjects,
    List<SkippedCell> skippedChapters,
    int failedCount
) {

    /**
     * 풀 부족 또는 셀 단위 실패로 스킵된 (Chapter, School) 셀 정보.
     */
    public record SkippedCell(
        Long chapterId,
        Long schoolId,
        String reason
    ) {
    }

    /**
     * 프로젝트는 생성되었으나 멤버 슬롯이 일부만 채워진 경우. orphan project 잔존 사실을 호출자에게
     * 명시적으로 노출한다.
     *
     * @param projectId          생성된 프로젝트 ID
     * @param chapterId          프로젝트의 chapter
     * @param schoolId           풀 추출에 사용한 school
     * @param addedMemberCount   실제로 add 된 멤버 수
     * @param expectedMemberCount 의도했던 멤버 슬롯 수
     * @param reason             실패 사유
     */
    public record PartialProject(
        Long projectId,
        Long chapterId,
        Long schoolId,
        int addedMemberCount,
        int expectedMemberCount,
        String reason
    ) {
    }
}
