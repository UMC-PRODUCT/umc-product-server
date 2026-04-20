package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

/**
 * 원본 워크북 상세 조회 결과 Info
 *
 * @param originalWorkbookId PK
 * @param title              원본 워크북 제목
 * @param description        원본 워크북 설명 (nullable)
 * @param url                원본 워크북 URL (nullable)
 * @param content            원본 워크북 본문 내용 (nullable)
 * @param type               워크북 유형 (MAIN / EXTRA)
 * @param status             워크북 상태 (DRAFT / READY / RELEASED)
 * @param releasedAt         배포 일시 (nullable)
 * @param releasedMemberId   배포 처리한 멤버 ID (nullable)
 * @param missions           포함된 워크북 미션 목록
 */
@Builder
public record OriginalWorkbookInfo(
    Long originalWorkbookId,
    String title,
    String description,
    String url,
    String content,
    OriginalWorkbookType type,
    OriginalWorkbookStatus status,
    Instant releasedAt,
    Long releasedMemberId,
    List<WorkbookMissionInfo> missions
) {

    /**
     * 원본 워크북 미션 정보
     *
     * @param originalWorkbookMissionId PK
     * @param title                     미션 제목
     * @param description               미션 설명 (nullable)
     * @param missionType               미션 제출 유형 (LINK / MEMO / PLAIN)
     * @param isNecessary               미션 필수 수행 여부
     */
    @Builder
    public record WorkbookMissionInfo(
        Long originalWorkbookMissionId,
        String title,
        String description,
        MissionType missionType,
        boolean isNecessary
    ) {
    }
}